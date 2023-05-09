import os
from dataclasses import dataclass
from datetime import datetime, timedelta
from re import search, sub

import boto3
from aws_lambda_powertools import Logger
from aws_lambda_powertools.utilities.data_classes import EventBridgeEvent

logger = Logger()
DYNAMODB_TABLE_NAME = os.environ['DYNAMODB_TABLE_NAME']
YAML_CONFIGURATION_FILE = 'suppressor.yml'

securityhub = boto3.client('securityhub')
table = boto3.resource('dynamodb').Table(name=DYNAMODB_TABLE_NAME)


@dataclass
class SuppressionRule:
    resources: [str]
    notes: str


def find_matching_check_and_rule(finding, suppression_rules):
    date_check_rules = r".*\[date_check (.*)\].*"
    now = datetime.now()

    for resource in finding.get('Resources', []):
        resource_id = resource['Id']
        for rule in suppression_rules:
            for resource_check in rule.resources:
                match = search(resource_check, resource_id)
                if match:
                    return resource_check, rule

                date_match = search(date_check_rules, resource_check)
                if date_match:
                    mins_difference = int(date_match.group())
                    date_to_check = now - timedelta(minutes=mins_difference)
                    new_resource_check = sub(date_check_rules, resource_check, r"(.*)")
                    new_match = search(new_resource_check, resource_id)
                    if new_match:
                        resource_date = datetime.strptime(new_match.group(), "%Y-%m-%d-%H-%M")
                        if resource_date > date_to_check:
                            return resource_check, rule
    return None, None


def suppress_in_securityhub(finding_id, product_arn, notes):
    now = datetime.now()

    securityhub.batch_update_findings(
        FindingIdentifiers=[{
            'Id': finding_id,
            'ProductArn': product_arn
        }],
        Workflow={'Status': 'SUPPRESSED'},
        Note={
            'Text': f'{notes} - Suppressed by the Security Hub Suppressor at {now.strftime("%Y-%m-%d %H:%M:%S")}',
            'UpdatedBy': 'suppression-lambda'
        }
    )


def suppress_finding(finding):
    title = finding['Title']
    product_arn = finding['ProductArn']
    finding_id = finding['Id']

    suppression_rules = list(map(
        lambda table_item: SuppressionRule(resources=table_item['resources'], notes=table_item['notes']),
        table.get_item(Key={"title": title}).get('Item', {}).get('data', {})
    ))

    if len(suppression_rules) == 0:
        logger.warning(f'No rules found for title: {title}')
        return None

    resource_check, rule = find_matching_check_and_rule(finding, suppression_rules)
    if resource_check:
        logger.info(
            f'Perform Suppression on finding {finding_id}, '
            f'matched resource check: {resource_check}, '
            f'with title: {title}'
        )
        suppress_in_securityhub(finding_id, product_arn, rule.notes)
        return finding_id

    return None


def suppress(event: EventBridgeEvent) -> []:
    findings = event['detail'].get('findings', [])
    suppressed_finding_ids = []
    for finding in findings:
        finding_id = finding['Id']
        if finding_id in suppressed_finding_ids:
            continue
        if suppress_finding(finding):
            suppressed_finding_ids.append(finding_id)
    return suppressed_finding_ids


def handle_event_bridge_event(event: EventBridgeEvent):
    suppressed_finding_ids = suppress(event)
    if len(suppressed_finding_ids) > 0:
        logger.info(f'Total findings processed: {len(suppressed_finding_ids)}')
        return {
            'finding_state': 'suppressed'
        }
    return {
        'finding_state': 'skipped'
    }
