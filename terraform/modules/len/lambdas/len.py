import logging
import os
import boto3

logger = logging.getLogger()
logger.setLevel(logging.INFO)

def lambda_handler(event, context):

    logger.info(f'Hello World!')
    logger.info(f'## ENVIRONMENT VARIABLES: {os.environ}')
    logger.info(f'## EVENT: {event}')

    return {
        'statusCode': 200,
    }
