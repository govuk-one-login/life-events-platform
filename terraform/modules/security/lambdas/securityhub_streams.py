# Code cloned originally from https://github.com/schubergphilis/terraform-aws-mcaf-securityhub-findings-manager/

import itertools
from typing import Dict

import boto3
from aws_lambda_powertools import Logger
from aws_lambda_powertools.utilities.data_classes import DynamoDBStreamEvent
from aws_lambda_powertools.utilities.data_classes.dynamo_db_stream_event import DynamoDBRecordEventName
from aws_lambda_powertools.utilities.data_classes import EventBridgeEvent

from securityhub_events import suppress

logger = Logger()
security_hub = boto3.client('securityhub')
paginator = security_hub.get_paginator('get_findings')


def get_findings(control_value: str) -> Dict[str, list]:
    findings = paginator.paginate(Filters={
        'Title': [
            {
                'Value': control_value,
                'Comparison': 'EQUALS'
            },
        ],
        'WorkflowStatus': [
            {
                'Value': 'NEW',
                'Comparison': 'EQUALS'
            },
            {
                'Value': 'NOTIFIED',
                'Comparison': 'EQUALS'
            }
        ]
    })
    return {'findings': list(itertools.chain.from_iterable([finding.get('Findings') for finding in findings]))}


def process_findings(findings_list):
    suppressed_findings_count = 0
    for finding in findings_list:
        try:
            event: EventBridgeEvent = {'detail': {'findings': [finding]}}
            if suppress(event):
                suppressed_findings_count += 1
        except ValueError:
            continue
    return suppressed_findings_count


def handle_stream_event(event: DynamoDBStreamEvent):
    total_findings = 0
    if event.records:
        for record in event.records:
            if record.event_name != DynamoDBRecordEventName.REMOVE:
                title = record.dynamodb.keys.get('title', {})
                findings_list = get_findings(title)
                if len(findings_list.get('findings')) == 0:
                    logger.warning(f'Could not find any findings with title {title}')
                    continue
                total_findings = process_findings(findings_list.get('findings'))
        logger.info(f'Total findings processed: {total_findings}')
