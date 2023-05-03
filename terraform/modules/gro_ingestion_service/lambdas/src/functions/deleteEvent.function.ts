import { EventRequest } from "../models/EventRequest"
import { DeleteEventResponse } from "../models/DeleteEventResponse"
import { DynamoDBClient, DeleteItemCommand, DeleteItemInput } from "@aws-sdk/client-dynamodb"
import { unmarshall } from "@aws-sdk/util-dynamodb"

const tableName = process.env.TABLE_NAME ?? ""

const dynamo = new DynamoDBClient({ apiVersion: "2012-08-10" })

export const handler = async (event: EventRequest): Promise<DeleteEventResponse> => {

    const params: DeleteItemInput = {
        Key: {
            hash: {
                S: event.id,
            }
        },
        TableName: tableName,
        ReturnValues: "ALL_OLD",
    }
    try {
        const command = new DeleteItemCommand(params)
        const result = await dynamo.send(command)

        let logParams = {
            hash: event.id,
            RegistrationId: "",
            EventTime: "",
        }
        if (result.Attributes) {
            const eventRecord = unmarshall(result.Attributes)
            logParams = {
                ...logParams,
                RegistrationId: eventRecord?.RegistrationId,
                EventTime: eventRecord?.EventTime,
            }
        }
        console.log("Successfully deleted event", logParams)
        return {
            id: event.id,
            statusCode: 204,
        }
    } catch (err) {
        const logParams = {
            hash: event.id,
            error: err,
        }
        console.error("Failed to delete event", logParams)
        return {
            statusCode: 404,
            id: event.id,
        }
    }
}
