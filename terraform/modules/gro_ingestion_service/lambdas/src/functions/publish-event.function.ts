import { DynamoDBStreamEvent, Handler } from "aws-lambda"
import { mapToEventRecord } from "../models/EventRecord"
import { request } from "https"
import { PublishEvent } from "../models/PublishEvent"
import { AttributeValue } from "aws-lambda/trigger/dynamodb-stream"

const apiUrl = process.env.API_URL
const options = {
    hostname: apiUrl,
    path: "/events",
    method: "POST",
    port: 443,
    headers: {
        "Content-Type": "application/json",
    },
}

const convertToEvent = (image: { [key: string]: AttributeValue }): PublishEvent => {
    const eventRecord = mapToEventRecord(image)
    return {
        eventType: "DEATH_NOTIFICATION",
        eventTime: eventRecord.EventTime,
        id: eventRecord.hash
    }
}

const publishEvent = async (event: PublishEvent) => new Promise((resolve, reject) => {
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
                reject({ error: err, event: event })
            }
        })
    })
    req.on("error", err => {
        reject(err)
    })

    req.write(event)
    req.end()
})

export const handler: Handler = async (event: DynamoDBStreamEvent) => {
    const publishEvents = event.Records
        .filter(r => r.eventName === "INSERT")
        .filter(r => r.dynamodb?.NewImage)
        .map(r => r.dynamodb?.NewImage)
        .map(convertToEvent)

    console.log(`Publishing ${publishEvents.length} events`)
    const results = await Promise.allSettled(publishEvents.map(publishEvent))
    const failedEvents = results.filter((r): r is PromiseRejectedResult => r.status === "rejected")
    if (failedEvents.length > 0) {
        throw new Error(`Failed to publish ${failedEvents.length} events`)
    }

    console.log("Succeeded publishing events")
}
