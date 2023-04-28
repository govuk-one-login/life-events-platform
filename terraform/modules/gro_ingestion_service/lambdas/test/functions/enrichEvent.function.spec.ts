import { describe, expect } from "@jest/globals"
import { EventRequest } from "../../src/models/EventRequest"
import { EnrichEventResponse } from "../../src/models/EnrichEventResponse"
import { handler } from "../../src/functions/enrichEvent.function"
import { awsSdkPromiseResponse, DocumentClient } from "../__mocks__/aws-sdk/clients/dynamodb"
import { dbItem } from "./dbItem"

const db = new DocumentClient()

describe("Unit test for enrich event handler", function () {
    test("verifies successful response", async () => {
        awsSdkPromiseResponse.mockReturnValueOnce(Promise.resolve({ Item: dbItem }))

        const event: EventRequest = {
            id: "hash1",
        }

        const result: EnrichEventResponse = await handler(event)

        expect(db.get).toHaveBeenCalledWith({ TableName: "", Key: { hash: event.id } })
        expect(result).toEqual({
            event: {
                hash: "abc",
                RegistrationId: "111",
                EventTime: "2023-01-11",
                DateOfDeath: "2023-01-01",
                FirstForename: "Forename",
                Surname: "Surname",
                Sex: "Male",
                DateOfBirth: "1945-01-02",
                AddressLine1: "1 Death Lane",
                AddressLine2: "",
                AddressLine3: "",
                AddressLine4: "",
                MaidenSurname: "",
                PartialMonthOfDeath: "Jan",
                PartialYearOfDeath: "2023",
                Postcode: "S11 9GH",
                VerificationLevel: "",
            },
            statusCode: 200,
        })
    })

    test("verifies failure response", async () => {
        awsSdkPromiseResponse.mockReturnValueOnce(Promise.resolve({ Item: null }))

        const event: EventRequest = {
            id: "hash1",
        }

        const result: EnrichEventResponse = await handler(event)

        expect(db.get).toHaveBeenCalledWith({ TableName: "", Key: { hash: event.id } })
        expect(result).toEqual({
            statusCode: 404,
        })
    })
})
