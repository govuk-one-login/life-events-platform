import json
import logging
import math
import os
from datetime import datetime
from random import randint
from urllib import request
import psycopg2

from common import get_auth_token

logger = logging.getLogger()
logger.setLevel(logging.INFO)

gdx_url = os.environ["gdx_url"]
events_url = gdx_url + "/events"
auth_url = os.environ["auth_url"]

client_id = os.environ["len_client_id"]
client_secret = os.environ["len_client_secret"]

lev_username = os.environ["lev_rds_db_username"]
lev_password = os.environ["lev_rds_db_password"]
lev_db_name = os.environ["lev_rds_db_name"]
lev_host = os.environ["lev_rds_db_host"]

forenames_options = ["Tester", None]
surname_options = ["SMITH", None]
date_of_birth_options = ["1912-02-29", None]
date_of_death_options = ["2012-02-29", None]
sex_options = ["Male", "Female", "Indeterminate", None]
address_options = ["10 Test Street", None]


def lambda_handler(event, context):
    logger.info(f"## EVENT: {event}")
    logger.info(f"## TIME: {datetime.now()}")

    auth_token = get_auth_token(auth_url, client_id, client_secret)

    if "detail-type" in event and event["detail-type"] == "Scheduled Event":
        run_scheduled_job(auth_token)
    else:
        run_manual_job(auth_token)

    return {
        "statusCode": 200,
    }


def run_scheduled_job(auth_token: str):
    now = datetime.now()
    minutes_into_day = (now - now.replace(hour=9, minute=0, second=0)).total_seconds() / 60
    number_of_events = math.floor((100 / (3 * math.pi)) * math.exp(-0.5 * (((minutes_into_day / 60) - 4.5) / 1.5) ** 2))

    for i in range(number_of_events):
        post_event(auth_token)


def run_manual_job(auth_token: str):
    post_event(auth_token)


def post_event(auth_token: str):
    death_registration_id = create_death_registration()
    event_request_data = json.dumps({
        "eventType": "DEATH_NOTIFICATION",
        "id": death_registration_id
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
    logger.info(f"## Posting death certificate {death_registration_id}")
    request.urlopen(event_request).read()
    logger.info(f"## Successfully posted death certificate {death_registration_id}")


def create_death_registration():
    connection = psycopg2.connect(user=lev_username, password=lev_password, database=lev_db_name, host=lev_host)
    cursor = connection.cursor()
    cursor.execute("select nextval('death_registration_ids')")
    death_registration_id = cursor.fetchone()[0]

    cursor.execute(
        """
        INSERT INTO death_registration_v1 (id, data, forenames, surname, date_of_birth, date_of_death)
        VALUES(%(id)s, %(data)s, %(forenames)s, %(surname)s, %(date_of_birth)s, %(date_of_death)s)
        """,
        generate_death_registration(death_registration_id))
    connection.commit()
    return death_registration_id


def generate_death_registration(death_registration_id: int):
    forenames = forenames_options[randint(0, len(forenames_options) - 1)]
    surname = surname_options[randint(0, len(surname_options) - 1)]
    date_of_birth = date_of_birth_options[randint(0, len(date_of_birth_options) - 1)]
    date_of_death = date_of_death_options[randint(0, len(date_of_death_options) - 1)]

    data = {
        "id": death_registration_id,
        "date": "2014-10-10",
        "entryNumber": death_registration_id,
        "registrar": {
            "signature": "A. Registrar",
            "designation": "Registrar",
            "subdistrict": "Test Subdistrict",
            "district": "Test District",
            "administrativeArea": "Reading"
        },
        "informant": {
            "forenames": "An",
            "surname": "Informant",
            "address": "1 Inform House",
            "qualification": "Doctor",
            "signature": "A. Informant"
        },
        "deceased": {
            "forenames": forenames,
            "surname": surname,
            "dateOfBirth": date_of_birth,
            "dateOfDeath": date_of_death,
            "ageAtDeath": "100 years",
            "birthplace": "Test address",
            "deathplace": "Test address",
            "sex": sex_options[randint(0, len(sex_options) - 1)],
            "address": address_options[randint(0, len(address_options) - 1)],
            "occupation": "Unemployed",
            "retired": False,
            "maidenSurname": "TESTER",
            "aliases": [],
            "causeOfDeath": "Old age",
            "certifiedBy": "A. Doctor MD",
        },
        "partner": {},
        "mother": {},
        "father": {},
        "coroner": {},
        "status": {
            "blocked": False,
            "correction": "None",
            "marginalNote": "None",
            "onAuthorityOfRegistrarGeneral": False
        },
    }
    return {
        "id": death_registration_id,
        "data": json.dumps(data),
        "forenames": forenames,
        "surname": surname,
        "date_of_birth": date_of_birth,
        "date_of_death": date_of_death,
    }
