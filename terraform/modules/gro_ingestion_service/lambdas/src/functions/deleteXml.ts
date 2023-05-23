import { DeleteObjectCommand, S3Client } from "@aws-sdk/client-s3"
import { Handler } from "aws-lambda"

import { BucketObjectDetails } from "../models/BucketObjectDetails"
import { LambdaFunction } from "../models/LambdaFunction"

const client = new S3Client({ apiVersion: "2012-08-10" })

const handler: Handler = async (event: BucketObjectDetails) => {
    const logParams: { fileKey: string; error?: Error } = {
        fileKey: event.key,
    }
    const deleteCommand = new DeleteObjectCommand({
        Bucket: event.bucket,
        Key: event.key,
    })

    try {
        await client.send(deleteCommand)
        console.log("Successfully deleted XML file", logParams)
    } catch (err) {
        logParams.error = err
        console.log("Failed to delete XML file", logParams)
    }
}

const lambdaFunction: LambdaFunction = {
    name: "deleteXml",
    handler: handler,
}
export default lambdaFunction
