import json
import logging
import os
from datetime import datetime
from http.client import HTTPResponse
from urllib import request

from common import get_auth_token

logger = logging.getLogger()
logger.setLevel(logging.INFO)

gdx_url = os.environ["gdx_url"]
events_url = gdx_url + "/events"

auth_url = os.environ["auth_url"]

client_id = os.environ["client_id"]
client_secret = os.environ["client_secret"]


def lambda_handler(event, _context):
    logger.info(f"## EVENT: {event}")
    logger.info(f"## TIME: {datetime.now()}")

    auth_token = get_auth_token(auth_url, client_id, client_secret)
    events = get_events(auth_token)

    logger.info(f"Received {len(events)} events")

    return {
        "event_ids": [event['eventId'] for event in events]
    }


def get_events(auth_token: str):
    events_request = request.Request(events_url, headers={"Authorization": "Bearer " + auth_token})
    response: HTTPResponse = request.urlopen(events_request)
    return json.loads(response.read())
