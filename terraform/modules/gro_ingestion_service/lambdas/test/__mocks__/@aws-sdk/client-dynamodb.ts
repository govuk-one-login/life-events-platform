export { GetItemCommand, GetItemInput, DeleteItemInput, DeleteItemCommand } from "@aws-sdk/client-dynamodb"

export const dynamoDbSendFn = jest.fn().mockReturnValue(Promise.resolve(true))

export class DynamoDBClient {
    send = dynamoDbSendFn
}
