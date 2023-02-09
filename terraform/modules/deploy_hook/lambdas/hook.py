import json
import logging
import os
import time
from datetime import datetime
from http.client import HTTPResponse
from typing import Literal
from urllib import request

import boto3

from common import get_auth_token

logger = logging.getLogger()
logger.setLevel(logging.INFO)

test_gdx_url = os.environ["test_gdx_url"]
test_auth_header = os.environ["test_auth_header"]
events_url = test_gdx_url + "/events"

auth_url = os.environ["auth_url"]

client_id = os.environ["client_id"]
client_secret = os.environ["client_secret"]

codedeploy = boto3.client("codedeploy")

max_retries = 5
delay = 5


def lambda_handler(event, _context):
    logger.info(f"## EVENT: {event}")
    logger.info(f"## TIME: {datetime.now()}")

    try:
        auth_token = get_auth_token(auth_url, client_id, client_secret)

        post_event(auth_token)
        events = get_events(auth_token)
        get_event(auth_token, events[0]["id"])
        delete_event(auth_token, events[0]["id"])

        put_lifecycle_event_hook(event, "Succeeded")
    except:
        put_lifecycle_event_hook(event, "Failed")
        raise


def put_lifecycle_event_hook(event, status: Literal["Failed", "Succeeded"]):
    deployment_id = event["DeploymentId"]
    lifecycle_event_hook_execution_id = event["LifecycleEventHookExecutionId"]

    codedeploy.put_lifecycle_event_hook_execution_status(
        deploymentId=deployment_id,
        lifecycleEventHookExecutionId=lifecycle_event_hook_execution_id,
        status=status
    )


def post_event(auth_token: str):
    event_request_data = json.dumps({
        "eventType": "TEST_EVENT",
        "id": 1
    }).encode("utf-8")
    event_request = request.Request(
        events_url,
        data=event_request_data,
        headers={
            "Authorization": "Bearer " + auth_token,
            "X-TEST-AUTH": test_auth_header,
            "Content-Type": "application/json; charset=utf-8",
            "Content-Length": len(event_request_data)
        }
    )
    logger.info(f"## Posting test event")
    request.urlopen(event_request).read()
    logger.info(f"## Successfully posted test event")


def get_events(auth_token: str, retries: int = 0):
    logger.info(f"## Getting test events")
    events = get_data(auth_token, events_url)["data"]
    logger.info(f"## Successfully retrieved {len(events)} test event(s)")
    if len(events) == 0:
        logger.info(f"## Retrieved no test events")
        if retries >= max_retries:
            raise Exception(f"Exceeded max retries of {max_retries} with a delay of {delay}")
        logger.info(f"## Retrying getting events in {delay} seconds")
        time.sleep(delay)
        events = get_events(auth_token, retries + 1)
    return events


def get_event(auth_token: str, event_id: str):
    logger.info(f"## Getting test event {event_id}")
    event = get_data(auth_token, f"{events_url}/{event_id}")
    logger.info(f"## Successfully retrieved event {event_id}")
    return event


def delete_event(auth_token: str, event_id: str):
    logger.info(f"## Deleting test event {event_id}")
    event_request = request.Request(
        f"{events_url}/{event_id}",
        headers={
            "Authorization": "Bearer " + auth_token,
            "X-TEST-AUTH": test_auth_header
        },
        method="DELETE"
    )
    request.urlopen(event_request)
    logger.info(f"## Successfully deleted event {event_id}")


def get_data(auth_token: str, url: str):
    event_request = request.Request(
        url,
        headers={
            "Authorization": "Bearer " + auth_token,
            "X-TEST-AUTH": test_auth_header,
            "Accept": "application/vnd.api+json"
        }
    )
    response: HTTPResponse = request.urlopen(event_request)
    return json.loads(response.read())
