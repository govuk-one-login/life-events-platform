import { DeleteObjectCommand, S3Client } from "@aws-sdk/client-s3"
import { Handler, } from "aws-lambda"
import { BucketObjectDetails } from "../models/BucketObjectDetails"

import { LambdaFunction } from "../models/LambdaFunction"

const client = new S3Client({ apiVersion: "2012-08-10" })

const handler: Handler = async (event: BucketObjectDetails) => {
    const deleteCommand = new DeleteObjectCommand({
        Bucket: event.bucket,
        Key: event.key,
    })

    await client.send(deleteCommand)
}

const lambdaFunction: LambdaFunction = {
    name: "deleteXml",
    handler: handler,
}
export default lambdaFunction
