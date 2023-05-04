import { DynamoDBClient, GetItemCommand, GetItemInput } from "@aws-sdk/client-dynamodb"
import { unmarshall } from "@aws-sdk/util-dynamodb"
import { Handler } from "aws-lambda"

import { EnrichEventResponse } from "../models/EnrichEventResponse"
import { EventRecord } from "../models/EventRecord"
import { EventRequest } from "../models/EventRequest"
import { LambdaFunction } from "../models/LambdaFunction"

const tableName = process.env.TABLE_NAME ?? ""

const dynamo = new DynamoDBClient({ apiVersion: "2012-08-10" })

const handler: Handler = async (event: EventRequest): Promise<EnrichEventResponse> => {
    const params: GetItemInput = {
        Key: {
            hash: {
                S: event.id,
            },
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

const lambdaFunction: LambdaFunction = {
    name: "enrichEvent",
    handler: handler
}
export default lambdaFunction
