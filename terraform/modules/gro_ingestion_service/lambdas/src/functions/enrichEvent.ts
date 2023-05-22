import { DynamoDBClient, GetItemCommand, GetItemInput } from "@aws-sdk/client-dynamodb"
import { unmarshall } from "@aws-sdk/util-dynamodb"
import { Handler } from "aws-lambda"

import { config } from "../helpers/config"
import { EventRecord } from "../models/EventRecord"
import { EventRequest } from "../models/EventRequest"
import { EnrichEventResponse } from "../models/EventResponse"
import { LambdaFunction } from "../models/LambdaFunction"

const dynamo = new DynamoDBClient({ apiVersion: "2012-08-10" })

const handler: Handler = async (event: EventRequest): Promise<EnrichEventResponse> => {
    const params: GetItemInput = {
        Key: {
            hash: {
                S: event.id,
            },
        },
        TableName: config.tableName,
    }

    const command = new GetItemCommand(params)

    try {
        const result = await dynamo.send(command)

        if (!result.Item) {
            return logError(event.id, `Record with hash ${event.id} not found`)
        }

        const eventRecord = unmarshall(result.Item) as EventRecord

        const logParams = {
            hash: eventRecord.hash,
            registrationId: eventRecord?.registrationId,
            eventTime: eventRecord?.eventTime,
            error: null,
        }

        console.log("Successfully enriched event", logParams)

        return {
            statusCode: 200,
            payload: eventRecord,
        }
    } catch (err) {
        return logError(event.id, err)
    }
}

const logError = (eventId, error): EnrichEventResponse => {
    const logParams = {
        hash: eventId,
        error: error,
    }

    console.error("Failed to enrich event", logParams)
    return {
        payload: eventId,
        statusCode: 404,
    }
}


const lambdaFunction: LambdaFunction = {
    name: "enrichEvent",
    handler: handler,
}
export default lambdaFunction
