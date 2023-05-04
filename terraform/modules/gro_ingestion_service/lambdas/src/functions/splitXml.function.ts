import { GetObjectCommand, S3Client } from "@aws-sdk/client-s3"
import { Handler, S3ObjectCreatedNotificationEvent } from "aws-lambda"
import { XMLParser } from "fast-xml-parser"

const client = new S3Client({ apiVersion: "2012-08-10" })
const parser = new XMLParser()

const getGroFile = async (event: S3ObjectCreatedNotificationEvent) => {
    const getGroCommand = new GetObjectCommand({
        Bucket: event.detail.bucket.name,
        Key: event.detail.object.key,
    })
    const groFileResponse = await client.send(getGroCommand)

    return groFileResponse.Body?.transformToString()
}

export const handler: Handler = async (event: S3ObjectCreatedNotificationEvent) => {
    const groXml = await getGroFile(event)

    if (!groXml) {
        const logParams = {
            fileKey: event.detail.object.key,
            error: `File with key ${event.detail.object.key} not found`,
        }
        console.error("Failed to insert records into DynamoDB", logParams)
        return {
            statusCode: 404,
        }
    }

    const groJson = parser.parse(groXml)
    return {
        bucket: event.detail.bucket.name,
        key: event.detail.object.key,
        deathRegistrations: groJson.deathRegistrationGroup,
    }
}
