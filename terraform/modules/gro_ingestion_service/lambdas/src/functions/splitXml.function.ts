import { Handler, S3Event } from "aws-lambda"
import { GetObjectCommand, S3Client } from "@aws-sdk/client-s3"
import { XMLParser } from "fast-xml-parser"
import { GroXmlFile } from "../models/GroXmlFile"

const getGroFile = async (event: S3Event) => {
    const client = new S3Client({})

    const getGroCommand = new GetObjectCommand({
        Bucket: event.Records[0].s3.bucket.name,
        Key: event.Records[0].s3.object.key
    })
    const groFileResponse = await client.send(getGroCommand)

    return groFileResponse.Body?.transformToString()
}

const parseGroXml = (groXml: string): GroXmlFile => {
    const parser = new XMLParser()

    return parser.parse(groXml)
}

export const handler: Handler = async (event: S3Event) => {
    const groFile = await getGroFile(event)

    return { deathRegistrations: parseGroXml(groFile ?? "").deathRegistrationGroup }
}
