import json
import logging
import os
from datetime import datetime
from http.client import HTTPResponse
from urllib import request

import boto3

from common import get_auth_token, record_metric

logger = logging.getLogger()
logger.setLevel(logging.INFO)

gdx_url = os.environ["gdx_url"]
events_url = gdx_url + "/events"

auth_url = os.environ["auth_url"]

client_id = os.environ["client_id"]
client_secret = os.environ["client_secret"]

codedeploy = boto3.client('codedeploy')


def lambda_handler(event, _context):
    logger.info(f"## EVENT: {event}")
    logger.info(f"## TIME: {datetime.now()}")

    put_lifecycle_event_hook(event, 'InProgress')

    auth_token = get_auth_token(auth_url, client_id, client_secret)

    post_event(auth_token)
    events = get_events(auth_token)
    get_event(auth_token, events[0]["id"])

    put_lifecycle_event_hook(event, 'Succeeded')


def put_lifecycle_event_hook(event, status: str):
    deployment_id = event.DeploymentId
    lifecycle_event_hook_execution_id = event.LifecycleEventHookExecutionId

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
            "Content-Type": "application/json; charset=utf-8",
            "Content-Length": len(event_request_data)
        }
    )
    logger.info(f"## Posting test event")
    request.urlopen(event_request).read()
    logger.info(f"## Successfully posted test event")


def get_events(auth_token: str):
    logger.info(f"## Getting test events")
    events = get_data(auth_token, events_url)["data"]
    logger.info(f"## Successfully retrieved {len(events)} test event(s)")
    return events


def get_event(auth_token: str, event_id: str):
    logger.info(f"## Getting test event {event_id}")
    event = get_data(auth_token, f"{events_url}/{event_id}")
    logger.info(f"## Successfully retrieved event {event_id}")
    return event


def get_data(auth_token: str, url: str):
    event_request = request.Request(
        url,
        headers={"Authorization": "Bearer " + auth_token, "Accept": "application/vnd.api+json"}
    )
    response: HTTPResponse = request.urlopen(event_request)
    return json.loads(response.read())
