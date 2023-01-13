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

lev_api_url = os.environ["lev_api_url"]


def lambda_handler(event, _context):
    logger.info(f"## EVENT: {event}")
    logger.info(f"## TIME: {datetime.now()}")

    auth_token = get_auth_token(auth_url, client_id, client_secret)
    retrieved_event = get_event(auth_token, event["event_id"])
    lev_record = get_lev_record(retrieved_event["sourceId"])

    for datum in retrieved_event["eventData"].keys():
        assert_matches_lev(retrieved_event, lev_record, datum)

    return retrieved_event


def get_event(auth_token: str, event_id: str):
    event_request = request.Request(f"{events_url}/{event_id}", headers={"Authorization": "Bearer " + auth_token})
    response: HTTPResponse = request.urlopen(event_request)
    return json.loads(response.read())


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
    provided_data = retrieved_event["eventData"][datum]
    lev_key = map_to_lev_key(datum)
    lev_data = lev_record["deceased"][lev_key]
    if provided_data != lev_data:
        logger.error(f"Data mismatch. Provided data: {provided_data}, lev key: {lev_key}, lev data: {lev_data}")


def map_to_lev_key(key: str) -> str:
    if key == "firstName":
        return "forenames"
    elif key == "lastName":
        return "surname"
    elif key == "dateOfBirth":
        return "dateOfBirth"
    elif key == "dateOfDeath":
        return "dateOfDeath"
    elif key == "address":
        return "address"
