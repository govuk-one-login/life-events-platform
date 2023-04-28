import { DynamoDBClient, PutItemCommand, PutItemCommandInput } from "@aws-sdk/client-dynamodb"
import { GroDeathRegistrationEvent } from "../models/GroDeathRegistrationEvent"
import hash from "object-hash"
import { GroDeathRegistration, mapToDynamoDbItem } from "../models/GroDeathRegistration"
import { Handler } from "aws-lambda"

const tableName = process.env.TABLE_NAME

const client = new DynamoDBClient({})

const pushRecord = async (record: PutItemCommandInput) => {
    const command = new PutItemCommand(record)
    await client.send(command)
}

const generateRecord = (deathRegistration: GroDeathRegistration): PutItemCommandInput => {
    const eventHash = hash(deathRegistration)
    return {
        Item: mapToDynamoDbItem(deathRegistration, eventHash),
        TableName: tableName,
    }
}

export const handler: Handler = async (event: GroDeathRegistrationEvent) => {
    const deathRecord = generateRecord(event.deathRegistration)
    await pushRecord(deathRecord)
}
