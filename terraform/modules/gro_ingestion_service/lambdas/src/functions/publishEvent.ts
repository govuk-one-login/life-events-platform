import { AttributeValue } from "@aws-sdk/client-dynamodb"
import { unmarshall } from "@aws-sdk/util-dynamodb"
import { DynamoDBStreamEvent, Handler } from "aws-lambda"
import { request, RequestOptions } from "https"

import { EventRecord } from "../models/EventRecord"
import { LambdaFunction } from "../models/LambdaFunction"
import { PublishEvent } from "../models/PublishEvent"

const gdxUrl = process.env.GDX_URL ?? ""
const authUrl = process.env.AUTH_URL ?? ""
const clientId = process.env.CLIENT_ID
const clientSecret = process.env.CLIENT_SECRET

const makeRequest = async (url: string, options: RequestOptions, requestData: string): Promise<object> =>
    new Promise((resolve, reject) => {
        const req = request(url, options, res => {
            res.setEncoding("utf8")
            let responseBody = ""

            res.on("data", chunk => {
                responseBody += chunk
            })

            res.on("end", () => {
                try {
                    resolve(JSON.parse(responseBody))
                } catch (err) {
                    reject({ err, statusCode: res.statusCode, responseBody: responseBody })
                }
            })
        })
        req.on("error", err => {
            reject(err)
        })

        req.write(requestData)
        req.end()
    })

const getAccessToken = async () => {
    const authRequest = JSON.stringify({
        grant_type: "client_credentials",
        client_id: clientId,
        client_secret: clientSecret,
    })

    const authOptions: RequestOptions = {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Content-Length": authRequest.length,
        },
    }

    const result = await makeRequest(authUrl, authOptions, authRequest)
    return result["access_token"]
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
    }

    return await makeRequest(gdxUrl, options, eventData)
        .then(response => ({ success: true, result: response, event }))
        .catch(error => ({ success: false, result: error, event }))
}

const handler: Handler = async (event: DynamoDBStreamEvent) => {
    const eventRecords = event.Records.filter(r => r.dynamodb?.NewImage)
        .map(r => r.dynamodb?.NewImage)
        .map(r => unmarshall(r as Record<string, AttributeValue>) as EventRecord)
    const publishEvents = eventRecords.map(
        (r): PublishEvent => ({
            eventType: "DEATH_NOTIFICATION",
            eventTime: r.EventTime,
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
            RegistrationId: eventRecord?.RegistrationId,
            EventTime: eventRecord?.EventTime,
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
    handler: handler
}
export default lambdaFunction
