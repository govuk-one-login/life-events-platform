import { describe, expect } from "@jest/globals"

import lambdaFunction from "../../src/functions/deleteEvent"
import { config } from "../../src/helpers/config"
import { DeleteEventResponse } from "../../src/models/DeleteEventResponse"
import { DynamoDBClient, dynamoDbSendFn } from "../__mocks__/@aws-sdk/client-dynamodb"
import { mockCallback, mockContext } from "../const/aws-lambda"
import { dbItem } from "../const/dbItem"
import { eventRequest } from "../const/eventRequest"

const db = new DynamoDBClient()

describe("Unit test for delete event handler", function () {
    test("verifies successful response", async () => {
        dynamoDbSendFn.mockReturnValueOnce(Promise.resolve({ Attributes: dbItem }))

        const result: DeleteEventResponse = await lambdaFunction.handler(eventRequest, mockContext, mockCallback)

        expect(db.send).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Key: {
                        hash: {
                            S: eventRequest.id,
                        },
                    },
                    TableName: config.tableName,
                    ReturnValues: "ALL_OLD",
                },
            }),
        )
        expect(result).toEqual({
            id: eventRequest.id,
            statusCode: 200,
        })
    })

    test("verifies delete failure response", async () => {
        dynamoDbSendFn.mockImplementation(() => {
            throw new Error()
        })

        const result: DeleteEventResponse = await lambdaFunction.handler(eventRequest, mockContext, mockCallback)

        expect(db.send).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Key: {
                        hash: {
                            S: eventRequest.id,
                        },
                    },
                    TableName: config.tableName,
                    ReturnValues: "ALL_OLD",
                },
            }),
        )
        expect(result).toEqual({
            id: eventRequest.id,
            statusCode: 404,
        })
    })
})
