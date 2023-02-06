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

lev_api_url = os.environ["lev_api_url"]

cloudwatch = boto3.client("cloudwatch")
cloudwatch_namespace = os.environ["cloudwatch_metric_namespace"]

sqs = boto3.client("sqs")
queue_name = os.environ["queue_name"]
queue_url = sqs.get_queue_url(QueueName=queue_name)["QueueUrl"]


def lambda_handler(event, _context):
    logger.info(f"## EVENT: {event}")
    logger.info(f"## TIME: {datetime.now()}")

    auth_token = get_auth_token(auth_url, client_id, client_secret)
    retrieved_event = get_event(auth_token, event["event_id"])
    lev_record = get_lev_record(retrieved_event["data"]["attributes"]["sourceId"])

    for datum in retrieved_event["data"]["attributes"]["eventData"].keys():
        assert_matches_lev(retrieved_event, lev_record, datum)

    push_event_to_queue(retrieved_event)

    return {"event_id": retrieved_event["data"]["id"]}


def get_event(auth_token: str, event_id: str):
    event_request = request.Request(f"{events_url}/{event_id}", headers={"Authorization": "Bearer " + auth_token})
    start = time.time()
    response: HTTPResponse = request.urlopen(event_request)
    stop = time.time()
    delta = stop - start
    record_metric(cloudwatch, cloudwatch_namespace, "GET_EVENT.Duration", delta, unit="Seconds")
    return json.loads(response.read())


def push_event_to_queue(event):
    queue_event = event["data"]["attributes"]
    queue_event["id"] = event["data"]["id"]
    sqs.send_message(
        QueueUrl=queue_url,
        MessageBody=json.dumps(queue_event)
    )


def get_lev_record(record_id: str):
    record_request = request.Request(
        f"{lev_api_url}/v1/registration/death/{record_id}",
        headers={"X-Auth-Aud": "gdx-data-share", "X-Auth-Username": "gdx-data-share-user"}
    )
    response: HTTPResponse = request.urlopen(record_request)
    return json.loads(response.read())


def assert_matches_lev(retrieved_event, lev_record, datum):
    """Fetch the matching record from the LEV API and assert matches.

    This is not needed in a normal consumer. Only for validation."""
    provided_data = retrieved_event["data"]["attributes"]["eventData"][datum]

    if provided_data is None:
        return check_missing_field(datum, lev_record, provided_data)

    if datum == "registrationDate":
        lev_key = "date"
        lev_data = lev_record["date"]
    else:
        lev_key = map_to_lev_key(datum)
        lev_data = lev_record["deceased"][lev_key]

    if provided_data != lev_data:
        logger.error(f"Data mismatch. Provided data: {provided_data}, lev key: {lev_key}, lev data: {lev_data}")
        record_metric(cloudwatch, cloudwatch_namespace, "GET_EVENT.DataMatchFailure", 1)
    else:
        record_metric(cloudwatch, cloudwatch_namespace, "GET_EVENT.DataMatchSuccess", 1)


def check_missing_field(datum, lev_record, provided_data):
    # LEV API will sometimes omit a null field entirely from the response
    lev_key = map_to_lev_key(datum)
    if lev_key in lev_record["deceased"] and lev_record["deceased"][lev_key] is not None:
        lev_data = lev_record["deceased"][datum]
        logger.error(f"Data mismatch. Provided data: {provided_data}, lev key: {lev_key}, lev data: {lev_data}")
        record_metric(cloudwatch, cloudwatch_namespace, "GET_EVENT.DataMatchFailure", 1)
    else:
        record_metric(cloudwatch, cloudwatch_namespace, "GET_EVENT.DataMatchSuccess", 1)


def map_to_lev_key(key: str) -> str:
    if key == "firstNames":
        return "forenames"
    elif key == "lastName":
        return "surname"
    elif key == "dateOfBirth":
        return "dateOfBirth"
    elif key == "dateOfDeath":
        return "dateOfDeath"
    elif key == "address":
        return "address"
    else:
        return key
