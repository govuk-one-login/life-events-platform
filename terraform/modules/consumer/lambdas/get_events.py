import json
import logging
import os
import time
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

cloudwatch = boto3.client("cloudwatch")
cloudwatch_namespace = os.environ["cloudwatch_metric_namespace"]


def lambda_handler(event, _context):
    logger.info(f"## EVENT: {event}")
    logger.info(f"## TIME: {datetime.now()}")

    auth_token = get_auth_token(auth_url, client_id, client_secret)
    start = time.time()
    events = get_events(auth_token)
    stop = time.time()
    delta = stop - start

    record_metric(cloudwatch, cloudwatch_namespace, "GET_EVENTS.Duration", delta, unit="Seconds")
    record_metric(cloudwatch, cloudwatch_namespace, "GET_EVENTS.Count", len(events))

    logger.info(f"Received {len(events)} events in {delta}s")

    return {
        "event_ids": [event['eventId'] for event in events]
    }


def get_events(auth_token: str):
    events_request = request.Request(events_url, headers={"Authorization": "Bearer " + auth_token})
    response: HTTPResponse = request.urlopen(events_request)
    return json.loads(response.read())
