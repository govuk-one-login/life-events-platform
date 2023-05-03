import { expect, describe } from "@jest/globals"
import { EventRequest } from "../../src/models/EventRequest"
import { DeleteEventResponse } from "../../src/models/DeleteEventResponse"
import { handler } from "../../src/functions/deleteEvent.function"
import { dbItem } from "../const/dbItem"

import { DynamoDBClient, dynamoDbSendFn } from "../__mocks__/@aws-sdk/client-dynamodb"

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
