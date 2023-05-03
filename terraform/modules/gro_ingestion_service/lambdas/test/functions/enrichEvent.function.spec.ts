import { describe, expect } from "@jest/globals"

import { handler } from "../../src/functions/enrichEvent.function"
import { EnrichEventResponse } from "../../src/models/EnrichEventResponse"
import { DynamoDBClient, dynamoDbSendFn } from "../__mocks__/@aws-sdk/client-dynamodb"
import { dbItem } from "../const/dbItem"
import { eventRequest } from "../const/eventRequest"

const db = new DynamoDBClient()

describe("Unit test for app handler", function () {
    test("verifies successful response", async () => {
        dynamoDbSendFn.mockReturnValueOnce(Promise.resolve({ Item: dbItem }))

        const result: EnrichEventResponse = await handler(eventRequest)

        expect(db.send).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Key: {
                        hash: {
                            S: eventRequest.id,
                        },
                    },
                    TableName: "",
                },
            }),
        )

        expect(result).toEqual({
            event: {
                hash: eventRequest.id,
                RegistrationId: "111",
                EventTime: "2023-01-11",
                DateOfDeath: "2023-01-01",
                FirstForename: "Forename",
                Surname: "Surname",
                Sex: "Male",
                DateOfBirth: "1945-01-02",
                AddressLine1: "1 Death Lane",
                AddressLine2: null,
                AddressLine3: null,
                AddressLine4: null,
                MaidenSurname: null,
                PartialMonthOfDeath: "Jan",
                PartialYearOfDeath: "2023",
                Postcode: "S11 9GH",
                VerificationLevel: null,
            },
            statusCode: 200,
        })
    })
})
