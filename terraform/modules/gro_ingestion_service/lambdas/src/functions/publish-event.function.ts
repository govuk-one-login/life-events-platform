import {DynamoDBRecord, DynamoDBStreamEvent, Handler} from "aws-lambda"
import {mapToEventRecord} from "../models/EventRecord"
import {request} from "https"

const apiUrl = process.env.API_URL
const options = {
    hostname: apiUrl,
    path: "/todos",
    method: "POST",
    port: 443,
    headers: {
        "Content-Type": "application/json",
    },
}

const publishRecord = async (record: DynamoDBRecord) => {
    if (record.eventName !== "INSERT" || !record.dynamodb?.NewImage) {
        return
    }

    const eventRecord = mapToEventRecord(record.dynamodb.NewImage)
    const event = {
        eventType: "DEATH_NOTIFICATION",
        eventTime: eventRecord.EventTime,
        id: eventRecord.hash
    }

    return new Promise((resolve, reject) => {
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

        req.write(event)
        req.end()
    })
}

export const handler: Handler = async (event: DynamoDBStreamEvent) => {
    console.log(`Streaming ${event.Records.filter(r => r.eventName === "INSERT").length} events`)
    await Promise.all(event.Records.map(publishRecord))
}
