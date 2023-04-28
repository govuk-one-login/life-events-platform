import { expect, describe } from "@jest/globals"
import { EventRequest } from "../../src/models/EventRequest"
import { DeleteEventResponse } from "../../src/models/DeleteEventResponse"
import { handler } from "../../src/functions/deleteEvent.function"
import { awsSdkPromiseResponse, DocumentClient } from "../__mocks__/aws-sdk/clients/dynamodb"
import { dbItem } from "./dbItem"

const db = new DocumentClient()

describe("Unit test for delete event handler", function () {
    test("verifies successful response", async () => {
        awsSdkPromiseResponse.mockReturnValueOnce(Promise.resolve({ Attributes: dbItem }))

        const event: EventRequest = {
            id: "hash1",
        }

        const result: DeleteEventResponse = await handler(event)

        expect(db.delete).toHaveBeenCalledWith({
            TableName: "",
            Key: { hash: event.id },
            ReturnValues: "ALL_OLD",
        })
        expect(result).toEqual({
            id: event.id,
            statusCode: 204,
        })
    })

    test("verifies delete failure response", async () => {
        awsSdkPromiseResponse.mockImplementation(() => {
            throw new Error()
        })

        const event: EventRequest = {
            id: "hash1",
        }

        const result: DeleteEventResponse = await handler(event)

        expect(db.delete).toHaveBeenCalledWith({
            TableName: "",
            Key: { hash: event.id },
            ReturnValues: "ALL_OLD",
        })
        expect(result).toEqual({
            id: event.id,
            statusCode: 404,
        })
    })
})
