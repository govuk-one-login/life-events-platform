export * from "@aws-sdk/client-s3"

export const s3SendFn = jest.fn()

export class S3Client {
    send = s3SendFn
}
