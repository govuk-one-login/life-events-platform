import { DocumentClient } from "aws-sdk/clients/dynamodb"
import { mapToEventRecord } from "../models/EventRecord"
import { EnrichEventRequest } from "../models/EnrichEventRequest"
import { EnrichEventResponse } from "../models/EnrichEventResponse"

const tableName: string = process.env.TABLE_NAME ?? ""

const dynamo: DocumentClient = new DocumentClient({ apiVersion: "2012-08-10" })

export const handler = async (event: EnrichEventRequest): Promise<EnrichEventResponse> => {

    const params: DocumentClient.GetItemInput = {
        Key: {
            hash: event.id
        },
        TableName: tableName
    }

    const result = await dynamo.get(params).promise()

    if (!result.Item) {
        const logParams = {
            hash: event.id,
            error: `Record with hash ${event.id} not found`,
        }
        console.error("Failed to enrich event", logParams)
        return {
            statusCode: 404
        }
    }

    const eventRecord = mapToEventRecord(result.Item)

    const logParams = {
        hash: eventRecord.hash,
        RegistrationId: eventRecord?.RegistrationId,
        EventTime: eventRecord?.EventTime,
        error: null,
    }

    console.log("Successfully enriched event", logParams)

    return {
        statusCode: 200,
        event: eventRecord
    }
}
