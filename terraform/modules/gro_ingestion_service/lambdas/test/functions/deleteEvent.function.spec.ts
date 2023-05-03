import { describe, expect } from "@jest/globals"

import { handler } from "../../src/functions/deleteEvent.function"
import { DeleteEventResponse } from "../../src/models/DeleteEventResponse"
import { EventRequest } from "../../src/models/EventRequest"
import { DynamoDBClient, dynamoDbSendFn } from "../__mocks__/@aws-sdk/client-dynamodb"
import { dbItem } from "../const/dbItem"

const db = new DynamoDBClient()

describe("Unit test for delete event handler", function () {
    test("verifies successful response", async () => {
        dynamoDbSendFn.mockReturnValueOnce(Promise.resolve({ Attributes: dbItem }))

        const event: EventRequest = {
            id: "hash1",
        }

        const result: DeleteEventResponse = await handler(event)

        expect(db.send).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Key: {
                        hash: {
                            S: event.id,
                        },
                    },
                    TableName: "",
                    ReturnValues: "ALL_OLD",
                },
            }),
        )
        expect(result).toEqual({
            id: event.id,
            statusCode: 200,
        })
    })

    test("verifies delete failure response", async () => {
        dynamoDbSendFn.mockImplementation(() => {
            throw new Error()
        })

        const event: EventRequest = {
            id: "hash1",
        }

        const result: DeleteEventResponse = await handler(event)

        expect(db.send).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Key: {
                        hash: {
                            S: event.id,
                        },
                    },
                    TableName: "",
                    ReturnValues: "ALL_OLD",
                },
            }),
        )
        expect(result).toEqual({
            id: event.id,
            statusCode: 404,
        })
    })
})
