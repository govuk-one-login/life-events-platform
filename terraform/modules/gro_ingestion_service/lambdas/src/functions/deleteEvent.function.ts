import { EventRequest } from "../models/EventRequest"
import { DeleteEventResponse } from "../models/DeleteEventResponse"
import { mapToEventRecord } from "../models/EventRecord"
import { DocumentClient } from "aws-sdk/clients/dynamodb"

const tableName = process.env.TABLE_NAME ?? ""

const dynamo = new DocumentClient({ apiVersion: "2012-08-10" })

export const handler = async (event: EventRequest): Promise<DeleteEventResponse> => {
    const deleteRequest: DocumentClient.DeleteItemInput = {
        Key: {
            hash: event.id,
        },
        TableName: tableName,
        ReturnValues: "ALL_OLD",
    }

    try {
        const result = await dynamo.delete(deleteRequest).promise()

        let logParams = {
            hash: event.id,
            RegistrationId: "",
            EventTime: "",
        }
        if (result.Attributes) {
            const eventRecord = mapToEventRecord(result.Attributes)
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
