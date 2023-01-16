import logging
import os
import time
from datetime import datetime
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
    delete_event(auth_token, event["event_id"])


def delete_event(auth_token: str, event_id: str):
    event_request = request.Request(
        f"{events_url}/{event_id}",
        headers={"Authorization": "Bearer " + auth_token},
        method="DELETE"
    )
    start = time.time()
    request.urlopen(event_request)
    stop = time.time()
    delta = stop - start
    record_metric(cloudwatch, cloudwatch_namespace, "DELETE_EVENT.Duration", delta, unit="Seconds")
