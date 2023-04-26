import { DynamoDBStreamEvent, Handler } from "aws-lambda"
import { mapToEventRecord } from "../models/EventRecord"
import { request, RequestOptions } from "https"
import { PublishEvent } from "../models/PublishEvent"
import { AttributeValue } from "aws-lambda/trigger/dynamodb-stream"

const apiUrl = process.env.API_URL
const authUrl = process.env.AUTH_URL
const clientId = process.env.CLIENT_ID
const clientSecret = process.env.CLIENT_SECRET

const makeRequest = async (options: RequestOptions, requestData: string): Promise<object> => new Promise((resolve, reject) => {
    const req = request(options, res => {
        res.setEncoding("utf8")
        let responseBody = ""

        res.on("data", (chunk) => {
            responseBody += chunk
        })

        res.on("end", () => {
            try {
                resolve(JSON.parse(responseBody))
            } catch (err) {
                reject(err)
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
        client_secret: clientSecret
    })

    const authOptions: RequestOptions = {
        path: authUrl,
        method: "POST",
        port: 443,
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Content-Length": authRequest.length
        },
    }

    const result = await makeRequest(authOptions, authRequest)
    return result["access_token"]
}

const convertToEvent = (image: { [key: string]: AttributeValue }): PublishEvent => {
    const eventRecord = mapToEventRecord(image)
    return {
        eventType: "DEATH_NOTIFICATION",
        eventTime: eventRecord.EventTime,
        id: eventRecord.hash
    }
}

const publishEvent = async (event: PublishEvent, accessToken: string) => {
    const eventData = JSON.stringify(event)

    const options: RequestOptions = {
        hostname: apiUrl,
        path: "/events",
        method: "POST",
        port: 443,
        headers: {
            "Authorization": `Bearer ${accessToken}`,
            "Content-Type": "application/json; charset=utf-8",
            "Content-Length": eventData.length
        },
    }

    await makeRequest(options, eventData)
}

export const handler: Handler = async (event: DynamoDBStreamEvent) => {
    const publishEvents = event.Records
        .filter(r => r.dynamodb?.NewImage)
        .map(r => r.dynamodb?.NewImage)
        .map(convertToEvent)

    const accessToken = await getAccessToken()

    console.log(`Publishing ${publishEvents.length} events`)
    const results = await Promise.allSettled(
        publishEvents.map(e => publishEvent(e, accessToken))
    )
    const failedEvents = results
        .filter((r): r is PromiseRejectedResult => r.status === "rejected")

    if (failedEvents.length > 0) {
        throw new Error(`Failed to publish ${failedEvents.length} events`)
    }

    console.log("Succeeded publishing events")
}
