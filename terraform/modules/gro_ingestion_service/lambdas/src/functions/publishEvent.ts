import { AttributeValue } from "@aws-sdk/client-dynamodb"
import { unmarshall } from "@aws-sdk/util-dynamodb"
import { DynamoDBStreamEvent, Handler } from "aws-lambda"
import { RequestOptions } from "https"
import * as querystring from "querystring"

import { config } from "../helpers/config"
import { makeRequest } from "../helpers/https"
import { EventRecord } from "../models/EventRecord"
import { LambdaFunction } from "../models/LambdaFunction"
import { PublishEvent } from "../models/PublishEvent"

const getAccessToken = async () => {
    const authRequest = querystring.stringify({
        grant_type: "client_credentials",
        client_id: config.clientId,
        client_secret: config.clientSecret,
    })

    const authOptions: RequestOptions = {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Content-Length": authRequest.length,
        },
    }

    const result = await makeRequest(config.authUrl, authOptions, authRequest)
    const response = JSON.parse(result.responseBody)
    return response["access_token"]
}

const publishEvent = async (event: PublishEvent, accessToken: string) => {
    const eventData = JSON.stringify(event)

    const options: RequestOptions = {
        method: "POST",
        headers: {
            Authorization: `Bearer ${accessToken}`,
            "Content-Type": "application/json; charset=utf-8",
            "Content-Length": eventData.length,
        },
        path: "/events",
    }

    return await makeRequest(config.gdxUrl, options, eventData)
        .then(response => ({ success: true, result: response, event }))
        .catch(error => ({ success: false, result: error, event }))
}

const handler: Handler = async (event: DynamoDBStreamEvent) => {
    const eventRecords = event.Records.filter(r => r.dynamodb?.NewImage)
        .map(r => r.dynamodb?.NewImage)
        .map(r => unmarshall(r as Record<string, AttributeValue>) as EventRecord)
    const publishEvents = eventRecords.map(
        (r): PublishEvent => ({
            eventType: "GRO_DEATH_NOTIFICATION",
            eventTime: r.eventTime,
            id: r.hash,
        }),
    )

    const accessToken = await getAccessToken()

    console.log(`Publishing ${publishEvents.length} events`)
    const results = await Promise.all(publishEvents.map(e => publishEvent(e, accessToken)))

    let failedCount = 0
    results.forEach(({ success, result, event }) => {
        const eventRecord = eventRecords.find(r => r.hash === event.id)
        const logParams = {
            hash: eventRecord?.hash,
            registrationId: eventRecord?.registrationId,
            eventTime: eventRecord?.eventTime,
            error: null,
        }

        if (success) {
            return console.log("Successfully published event", logParams)
        }

        failedCount += 1
        logParams.error = result
        console.error("Failed to publish event", logParams)
    })

    if (failedCount > 0) {
        throw new Error(`Failed to publish ${failedCount} events`)
    }

    console.log(`Succeeded publishing ${results.length} events`)
}

const lambdaFunction: LambdaFunction = {
    name: "publishEvent",
    handler: handler,
}
export default lambdaFunction
