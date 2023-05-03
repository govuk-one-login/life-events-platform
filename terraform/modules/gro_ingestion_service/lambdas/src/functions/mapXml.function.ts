import { DynamoDBClient, PutItemCommand, PutItemCommandInput } from "@aws-sdk/client-dynamodb"
import { GroDeathRegistrationEvent } from "../models/GroDeathRegistrationEvent"
import { GroDeathRegistration, mapToEventRecord } from "../models/GroDeathRegistration"
import { Handler } from "aws-lambda"
import { marshall } from "@aws-sdk/util-dynamodb"

const tableName = process.env.TABLE_NAME

const client = new DynamoDBClient({ apiVersion: "2012-08-10" })

const pushRecord = async (record: PutItemCommandInput) => {
    const command = new PutItemCommand(record)
    await client.send(command)
}

const generateRecord = (deathRegistration: GroDeathRegistration): PutItemCommandInput => {
    const eventRecord = mapToEventRecord(deathRegistration)
    return {
        Item: marshall(eventRecord),
        TableName: tableName,
    }
}

export const handler: Handler = async (event: GroDeathRegistrationEvent) => {
    const deathRecord = generateRecord(event.deathRegistration)
    await pushRecord(deathRecord)
}
