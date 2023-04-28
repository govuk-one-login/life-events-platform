import { mapToEventRecord } from "../models/EventRecord"
import { EnrichEventRequest } from "../models/EnrichEventRequest"
import { EnrichEventResponse } from "../models/EnrichEventResponse"
import { DynamoDBClient, GetItemCommand, GetItemInput } from "@aws-sdk/client-dynamodb"

const tableName = process.env.TABLE_NAME ?? ""

const dynamo = new DynamoDBClient({ apiVersion: "2012-08-10" })

export const handler = async (event: EnrichEventRequest): Promise<EnrichEventResponse> => {

    const params: GetItemInput = {
        Key: {
            hash: {
                S: event.id,
            }
        },
        TableName: tableName,
    }

    const command = new GetItemCommand(params)
    const result = await dynamo.send(command)

    if (!result.Item) {
        const logParams = {
            hash: event.id,
            error: `Record with hash ${event.id} not found`,
        }
        console.error("Failed to enrich event", logParams)
        return {
            statusCode: 404,
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
        event: eventRecord,
    }
}
