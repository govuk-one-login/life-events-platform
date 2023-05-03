import { DeleteItemCommand, DeleteItemInput, DynamoDBClient } from "@aws-sdk/client-dynamodb"
import { unmarshall } from "@aws-sdk/util-dynamodb"

import { DeleteEventResponse } from "../models/DeleteEventResponse"
import { EventRequest } from "../models/EventRequest"

const tableName = process.env.TABLE_NAME ?? ""

const dynamo = new DynamoDBClient({ apiVersion: "2012-08-10" })

export const handler = async (event: EventRequest): Promise<DeleteEventResponse> => {
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
        const eventRecord = unmarshall(result.Attributes)
        const logParams = {
            hash: event.id,
            RegistrationId: eventRecord?.RegistrationId,
            EventTime: eventRecord?.EventTime,
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
