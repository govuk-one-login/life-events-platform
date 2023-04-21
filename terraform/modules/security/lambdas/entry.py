from typing import Any
from typing import Dict

from aws_lambda_powertools import Logger
from aws_lambda_powertools.utilities.typing import LambdaContext
from aws_lambda_powertools.utilities.data_classes import DynamoDBStreamEvent
from aws_lambda_powertools.utilities.data_classes import EventBridgeEvent

from securityhub_events import handle_event_bridge_event
from securityhub_streams import handle_stream_event

logger = Logger()


@logger.inject_lambda_context(log_event=True)
def lambda_handler(event: Dict[str, Any], context: LambdaContext):
    if 'Records' in event.keys():
        logger.info(f'Handling DynamoDBStreamEvent')
        handle_stream_event(DynamoDBStreamEvent(event))
    elif 'source' in event.keys() and event['source'] == 'aws.securityhub':
        logger.info(f'Handling EventBridgeEvent')
        handle_event_bridge_event(EventBridgeEvent(event))
    else:
        logger.error(f'Event is neither of type EventBridgeEvent nor of type DynamoDBStreamEvent')
