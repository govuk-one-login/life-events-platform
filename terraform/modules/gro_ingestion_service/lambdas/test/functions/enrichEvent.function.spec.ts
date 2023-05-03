import { expect, describe } from "@jest/globals"
import { EventRequest } from "../../src/models/EventRequest"
import { EnrichEventResponse } from "../../src/models/EnrichEventResponse"
import { handler } from "../../src/functions/enrichEvent.function"
import { DynamoDBClient, dynamoDbSendFn } from "../__mocks__/@aws-sdk/client-dynamodb"

const db = new DynamoDBClient()

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
                S: "",
            },
            DateOfBirth: {
                S: "1945-01-02",
            },
            AddressLine1: {
                S: "1 Death Lane",
            },
            AddressLine2: {
                S: "",
            },
            AddressLine3: {
                S: "",
            },
            AddressLine4: {
                S: "",
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
                S: "",
            },
        }
        dynamoDbSendFn.mockReturnValueOnce(Promise.resolve({ Item: item }))

        const event: EventRequest = {
            id: "hash1",
        }

        const result: EnrichEventResponse = await handler(event)

        expect(db.send).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Key: {
                        hash: {
                            S: event.id,
                        },
                    },
                    TableName: "",
                },
            }),
        )

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
