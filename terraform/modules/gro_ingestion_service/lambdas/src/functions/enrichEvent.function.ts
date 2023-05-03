import { EventRecord } from "../models/EventRecord"
import { EventRequest } from "../models/EventRequest"
import { EnrichEventResponse } from "../models/EnrichEventResponse"
import { DynamoDBClient, GetItemCommand, GetItemInput } from "@aws-sdk/client-dynamodb"
import { unmarshall } from "@aws-sdk/util-dynamodb"

const tableName = process.env.TABLE_NAME ?? ""

const dynamo = new DynamoDBClient({ apiVersion: "2012-08-10" })

export const handler = async (event: EventRequest): Promise<EnrichEventResponse> => {

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

    const eventRecord = unmarshall(result.Item) as EventRecord

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
