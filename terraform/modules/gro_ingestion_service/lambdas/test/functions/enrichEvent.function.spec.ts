import { expect, describe } from "@jest/globals"
import { EnrichEventRequest } from "../../src/models/EnrichEventRequest"
import { EnrichEventResponse } from "../../src/models/EnrichEventResponse"
import { handler } from "../../src/functions/enrichEvent.function"
import { awsSdkPromiseResponse, DocumentClient } from "../__mocks__/aws-sdk/clients/dynamodb"

const db = new DocumentClient()

describe("Unit test for app handler", function () {
    test("verifies successful response", async () => {
        const item = {
            hash: {
                S: "abc",
            },
            RegistrationId: {
                S: "111",
            },
            EventTime: {
                S: "2023-01-11",
            },
            DateOfDeath: {
                S: "2023-01-01",
            },
            FirstForename: {
                S: "Forename",
            },
            Surname: {
                S: "Surname",
            },
            Sex: {
                S: "Male",
            },
            MaidenSurname: {
                S: null,
            },
            DateOfBirth: {
                S: "1945-01-02",
            },
            AddressLine1: {
                S: "1 Death Lane",
            },
            AddressLine2: {
                S: null,
            },
            AddressLine3: {
                S: null,
            },
            AddressLine4: {
                S: null,
            },
            PartialMonthOfDeath: {
                S: "Jan",
            },
            PartialYearOfDeath: {
                S: "2023",
            },
            Postcode: {
                S: "S11 9GH",
            },
            VerificationLevel: {
                S: null,
            },
        }
        awsSdkPromiseResponse.mockReturnValueOnce(Promise.resolve({ Item: item }))

        const event: EnrichEventRequest = {
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
})
