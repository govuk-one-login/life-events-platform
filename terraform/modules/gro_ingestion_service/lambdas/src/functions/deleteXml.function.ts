import { DeleteObjectCommand, S3Client } from "@aws-sdk/client-s3"
import { Handler, S3Event } from "aws-lambda"

const client = new S3Client({ apiVersion: "2012-08-10" })

export const handler: Handler = async (event: S3Event) => {
    const deleteCommand = new DeleteObjectCommand({
        Bucket: event.Records[0].s3.bucket.name,
        Key: event.Records[0].s3.object.key,
    })

    await client.send(deleteCommand)
}
