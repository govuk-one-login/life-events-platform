import { DynamoDBClient, PutItemCommand, PutItemCommandInput } from "@aws-sdk/client-dynamodb"
import { marshall } from "@aws-sdk/util-dynamodb"
import { Handler } from "aws-lambda"

import { GroDeathRegistration, mapToEventRecord } from "../models/GroDeathRegistration"
import { LambdaFunction } from "../models/LambdaFunction"

const tableName = process.env.TABLE_NAME

const client = new DynamoDBClient({ apiVersion: "2012-08-10" })

const pushRecord = async (record: PutItemCommandInput) => {
    const command = new PutItemCommand(record)
    await client.send(command)
}

const generateRecord = (deathRegistration: GroDeathRegistration): PutItemCommandInput => {
    const eventRecord = mapToEventRecord(deathRegistration)
    return {
        Item: marshall(eventRecord, {convertEmptyValues: false, removeUndefinedValues: false}),
        TableName: tableName,
    }
}

const handler: Handler = async (event: GroDeathRegistration) => {
    const deathRecord = generateRecord(event)
    await pushRecord(deathRecord)
}

const lambdaFunction: LambdaFunction = {
    name: "mapXml",
    handler: handler,
}
export default lambdaFunction
