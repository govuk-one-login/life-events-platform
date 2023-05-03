import { describe, expect } from "@jest/globals"

import { DynamoDBClient, dynamoDbSendFn } from "../__mocks__/@aws-sdk/client-dynamodb"
import { dbItem } from "../const/dbItem"
import { DeleteEventResponse } from "../../src/models/DeleteEventResponse"
import { handler } from "../../src/functions/deleteEvent.function"
import { eventRequest } from "../const/eventRequest"

const db = new DynamoDBClient()

describe("Unit test for delete event handler", function () {
    test("verifies successful response", async () => {
        dynamoDbSendFn.mockReturnValueOnce(Promise.resolve({ Attributes: dbItem }))

        const result: DeleteEventResponse = await handler(eventRequest)

        expect(db.send).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Key: {
                        hash: {
                            S: eventRequest.id,
                        },
                    },
                    TableName: "",
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

        const result: DeleteEventResponse = await handler(eventRequest)

        expect(db.send).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Key: {
                        hash: {
                            S: eventRequest.id,
                        },
                    },
                    TableName: "",
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
