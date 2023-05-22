import { describe, expect } from "@jest/globals"

import lambdaFunction from "../../src/functions/deleteEvent"
import { config } from "../../src/helpers/config"
import { DeleteEventResponse } from "../../src/models/EventResponse"
import { dynamoDbSendFn } from "../__mocks__/@aws-sdk/client-dynamodb"
import { mockCallback, mockContext } from "../const/aws-lambda"
import { dbItem } from "../const/dbItem"
import { eventRequest } from "../const/eventRequest"

jest.mock("../../src/helpers/config")

describe("Unit test for delete event handler", function () {
    beforeEach(() => {
        dynamoDbSendFn.mockReset()
    })

    test("verifies successful response", async () => {
        dynamoDbSendFn.mockReturnValueOnce(Promise.resolve({ Attributes: dbItem }))

        const result: DeleteEventResponse = await lambdaFunction.handler(eventRequest, mockContext, mockCallback)

        expect(dynamoDbSendFn).toHaveBeenCalledWith(
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
            payload: eventRequest.id,
            statusCode: 200,
        })
    })

    test("verifies delete failure response", async () => {
        dynamoDbSendFn.mockImplementation(() => {
            throw new Error()
        })

        const result: DeleteEventResponse = await lambdaFunction.handler(eventRequest, mockContext, mockCallback)

        expect(dynamoDbSendFn).toHaveBeenCalledWith(
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
            payload: eventRequest.id,
            statusCode: 500,
        })
    })
})
