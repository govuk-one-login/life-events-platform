import { DeleteItemCommand, DeleteItemInput, DynamoDBClient } from "@aws-sdk/client-dynamodb"
import { unmarshall } from "@aws-sdk/util-dynamodb"
import { Handler } from "aws-lambda"

import { DeleteEventResponse } from "../models/DeleteEventResponse"
import { EventRecord } from "../models/EventRecord"
import { EventRequest } from "../models/EventRequest"
import { LambdaFunction } from "../models/LambdaFunction"

const tableName = process.env.TABLE_NAME ?? ""

const dynamo = new DynamoDBClient({ apiVersion: "2012-08-10" })

const handler: Handler = async (event: EventRequest): Promise<DeleteEventResponse> => {
    const params: DeleteItemInput = {
        Key: {
            hash: {
                S: event.id,
            },
        },
        TableName: tableName,
        ReturnValues: "ALL_OLD",
    }

    try {
        const command = new DeleteItemCommand(params)
        const result = await dynamo.send(command)

        if (!result.Attributes) {
            return logError({
                hash: event.id,
            })
        }
        const eventRecord = unmarshall(result.Attributes) as EventRecord
        const logParams = {
            hash: event.id,
            registrationId: eventRecord?.registrationId,
            eventTime: eventRecord?.eventTime,
        }
        console.log("Successfully deleted event", logParams)
        return {
            id: event.id,
            statusCode: 200,
        }
    } catch (err) {
        return logError({
            hash: event.id,
            error: err,
        })
    }

    function logError(logParams) {
        console.error("Failed to delete event", logParams)
        return {
            id: event.id,
            statusCode: 404,
        }
    }
}

const lambdaFunction: LambdaFunction = {
    name: "deleteEvent",
    handler: handler,
}
export default lambdaFunction
