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
    """
    The implementation here is deliberately not optimal. For example the page size is very small to test pagination
    and create load on the system for testing purposes. This is designed to verify the system, rather than be a model
    implementation.
    """
    logger.info(f"## EVENT: {event}")
    logger.info(f"## TIME: {datetime.now()}")

    auth_token = get_auth_token(auth_url, client_id, client_secret)
    start = time.time()
    result_page = get_first_events_page(auth_token)
    stop = time.time()
    delta = stop - start

    total_events = result_page["meta"]["page"]["totalElements"]
    data = result_page["data"]
    event_ids = [event['id'] for event in data]

    while "next" in result_page["links"]:
        result_page = get_data(auth_token, result_page["links"]["next"])
        page_ids = [event["id"] for event in result_page["data"]]
        event_ids.extend(page_ids)

    record_metric(cloudwatch, cloudwatch_namespace, "GET_EVENTS.Duration", delta, unit="Seconds")
    record_metric(cloudwatch, cloudwatch_namespace, "GET_EVENTS.Count", total_events)

    logger.info(f"Received {len(event_ids)} events in {delta}s")
    if total_events != len(event_ids):
        logger.error(f"Expected {total_events} events but received {len(event_ids)}")

    print(event_ids)
    return {
        "event_ids": event_ids
    }


def get_first_events_page(auth_token: str):
    return get_data(auth_token, events_url + "?page%5Bsize%5D=100")


def get_data(auth_token: str, url: str):
    events_request = request.Request(url, headers={"Authorization": "Bearer " + auth_token,
                                                   "Accept": "application/vnd.api+json"})
    response: HTTPResponse = request.urlopen(events_request)
    return json.loads(response.read())
