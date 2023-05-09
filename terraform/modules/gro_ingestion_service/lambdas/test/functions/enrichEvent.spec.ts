import { describe, expect } from "@jest/globals"

import lambdaFunction from "../../src/functions/enrichEvent"
import { config } from "../../src/helpers/config"
import { EnrichEventResponse } from "../../src/models/EnrichEventResponse"
import { dynamoDbSendFn } from "../__mocks__/@aws-sdk/client-dynamodb"
import { mockCallback, mockContext } from "../const/aws-lambda"
import { dbItem } from "../const/dbItem"
import { eventRequest } from "../const/eventRequest"

jest.mock("../../src/helpers/config")

describe("Unit test for enrich event handler", function () {
    test("verifies successful response", async () => {
        dynamoDbSendFn.mockReturnValueOnce(Promise.resolve({ Item: dbItem }))

        const result: EnrichEventResponse = await lambdaFunction.handler(eventRequest, mockContext, mockCallback)

        expect(dynamoDbSendFn).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Key: {
                        hash: {
                            S: eventRequest.id,
                        },
                    },
                    TableName: config.tableName,
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
