import { Handler, S3Event } from "aws-lambda"
import { GetObjectCommand, S3Client } from "@aws-sdk/client-s3"
import { XMLParser } from "fast-xml-parser"

const client = new S3Client({})
const parser = new XMLParser()

const getGroFile = async (event: S3Event) => {

    const getGroCommand = new GetObjectCommand({
        Bucket: event.Records[0].s3.bucket.name,
        Key: event.Records[0].s3.object.key
    })
    const groFileResponse = await client.send(getGroCommand)

    return groFileResponse.Body?.transformToString()
}

export const handler: Handler = async (event: S3Event) => {
    const groXml = await getGroFile(event)

    if (!groXml) {
        const logParams = {
            fileKey: event.Records[0].s3.object.key,
            error: `File with key ${event.Records[0].s3.object.key} not found`,
        }
        console.error("Failed to insert records into DynamoDB", logParams)
        return {
            statusCode: 404,
        }
    }

    const groJson = parser.parse(groXml)
    return { deathRegistrations: groJson.deathRegistrationGroup }
}
