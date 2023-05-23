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
    const logParams: { hash: string; registrationId?: string; eventTime?: string; error?: string | Error } = {
        hash: event.id,
    }

    const command = new GetItemCommand(params)

    try {
        const result = await dynamo.send(command)

        if (!result.Item) {
            logParams.error = `Record with hash ${event.id} not found`

            console.error("Failed to enrich event", logParams)
            return {
                statusCode: 404,
            }
        }

        const eventRecord = unmarshall(result.Item) as EventRecord

        logParams.registrationId = eventRecord.registrationId
        logParams.eventTime = eventRecord.eventTime

        console.log("Successfully enriched event", logParams)

        return {
            statusCode: 200,
            payload: eventRecord,
        }
    } catch (err) {
        logParams.error = err

        console.error("Failed to enrich event", logParams)
        return {
            statusCode: 500,
        }
    }
}

const lambdaFunction: LambdaFunction = {
    name: "enrichEvent",
    handler: handler,
}
export default lambdaFunction
