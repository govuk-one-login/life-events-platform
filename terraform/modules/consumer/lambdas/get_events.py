import json
import logging
import os
from datetime import datetime
from http.client import HTTPResponse
from urllib import request, parse

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

    auth_token = get_auth_token()
    events = get_events(auth_token)

    return [event['eventId'] for event in events]


def get_auth_token():
    auth_request_data = parse.urlencode({
        "grant_type": "client_credentials",
        "client_id": client_id,
        "client_secret": client_secret
    }).encode()
    auth_request = request.Request(
        url=auth_url,
        headers={"Content-Type": "application/x-www-form-urlencoded"},
        method="POST",
        data=auth_request_data
    )
    auth_token = json.loads(request.urlopen(auth_request).read())["access_token"]
    return auth_token


def get_events(auth_token: str):
    events_request = request.Request(events_url, headers={"Authorization": "Bearer " + auth_token})
    response: HTTPResponse = request.urlopen(events_request)
    return json.loads(response.read())
