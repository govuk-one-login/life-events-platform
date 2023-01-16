import logging
import os
from datetime import datetime
from urllib import request

from common import get_auth_token

logger = logging.getLogger()
logger.setLevel(logging.INFO)

gdx_url = os.environ["gdx_url"]
events_url = gdx_url + "/events"

auth_url = os.environ["auth_url"]

client_id = os.environ["client_id"]
client_secret = os.environ["client_secret"]

lev_api_url = os.environ["lev_api_url"]


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
    request.urlopen(event_request)

