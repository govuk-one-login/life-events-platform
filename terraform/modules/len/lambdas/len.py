import logging
import os
from random import randint
from urllib import request, parse
import json

logger = logging.getLogger()
logger.setLevel(logging.INFO)

gdx_url = os.environ["gdx_url"]
events_url = gdx_url + "/events"
auth_url = os.environ["auth_url"]

client_id = os.environ["len_client_id"]
client_secret = os.environ["len_client_secret"]


def lambda_handler(event, context):
    logger.info(f"## EVENT: {event}")

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

    if "detail-type" in event and event["detail-type"] == "Scheduled Event":
        run_scheduled_job(auth_token)
    else:
        run_manual_job(auth_token)

    return {
        "statusCode": 200,
    }


def run_scheduled_job(auth_token: str):
    for i in range(randint(0, 8)):
        post_event(auth_token)


def run_manual_job(auth_token: str):
    post_event(auth_token)


def post_event(auth_token: str):
    death_certificate = 999999900 + randint(1, 72)
    event_request_data = json.dumps({
        "eventType": "DEATH_NOTIFICATION",
        "id": death_certificate
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
    logger.info(f"## Posting death certificate {death_certificate}")
    request.urlopen(event_request).read()
    logger.info(f"## Successfully posted death certificate {death_certificate}")
