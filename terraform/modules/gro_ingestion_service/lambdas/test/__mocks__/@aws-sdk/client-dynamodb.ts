export * from "@aws-sdk/client-dynamodb"

export const dynamoDbSendFn = jest.fn()

export class DynamoDBClient {
    send = dynamoDbSendFn
}
