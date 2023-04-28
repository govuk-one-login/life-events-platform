import { Handler, S3Event } from "aws-lambda"
import { DeleteObjectCommand, S3Client } from "@aws-sdk/client-s3"

const client = new S3Client({})

export const handler: Handler = async (event: S3Event) => {
    const deleteCommand = new DeleteObjectCommand({
        Bucket: event.Records[0].s3.bucket.name,
        Key: event.Records[0].s3.object.key,
    })

    await client.send(deleteCommand)
}
