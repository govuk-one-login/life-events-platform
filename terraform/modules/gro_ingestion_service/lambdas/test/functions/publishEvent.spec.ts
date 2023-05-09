import { marshall } from "@aws-sdk/util-dynamodb"
import { describe, expect } from "@jest/globals"
import { AttributeValue, DynamoDBStreamEvent } from "aws-lambda"

import lambdaFunction from "../../src/functions/publishEvent"
import { config } from "../../src/helpers/config"
import * as https from "../../src/helpers/https"
import { EventRecord } from "../../src/models/EventRecord"
import { mockCallback, mockContext } from "../const/aws-lambda"

jest.mock("../../src/helpers/config")

const firstEventRecord: EventRecord = {
    hash: "abcdefghi",
    registrationId: "registrationId",
    eventTime: "2020-09-01",
    verificationLevel: "1",
    dateOfDeath: "2020-09-01",
    partialMonthOfDeath: "2",
    partialYearOfDeath: "2020",
    forenames: "forenames",
    surname: "surname",
    maidenSurname: "maidenSurname",
    sex: "F",
    dateOfBirth: "2020-09-01",
    addressLine1: "addressLine1",
    addressLine2: "addressLine2",
    addressLine3: "addressLine3",
    addressLine4: "addressLine4",
    postcode: "postcode",
}
const secondEventRecord: EventRecord = {
    hash: "abcdefghij",
    registrationId: "registrationId",
    eventTime: "2020-09-01",
    verificationLevel: "1",
    dateOfDeath: "2020-09-01",
    partialMonthOfDeath: "2",
    partialYearOfDeath: "2020",
    forenames: "forenames",
    surname: "surname",
    maidenSurname: "maidenSurname",
    sex: "F",
    dateOfBirth: "2020-09-01",
    addressLine1: "addressLine1",
    addressLine2: "addressLine2",
    addressLine3: "addressLine3",
    addressLine4: "addressLine4",
    postcode: "postcode",
}

const streamEvent: DynamoDBStreamEvent = {
    Records: [
        {
            awsRegion: "eu-west-2",
            dynamodb: {
                NewImage: marshall(firstEventRecord) as Record<string, AttributeValue>,
            },
            eventName: "INSERT",
        },
        {
            awsRegion: "eu-west-2",
            dynamodb: {
                NewImage: marshall(secondEventRecord) as Record<string, AttributeValue>,
            },
            eventName: "INSERT",
        },
    ],
}

const mockAccessToken = "mock_access_token"
const makeRequestSpy = jest.spyOn(https, "makeRequest")
makeRequestSpy.mockImplementation(async url => {
    if (url == config.authUrl) {
        return {
            statusCode: 200,
            responseBody: `{"access_token": "${mockAccessToken}"}`,
        }
    }
    if (url == config.gdxUrl) {
        return {
            statusCode: 200,
            responseBody: "",
        }
    }
    throw Error(`url not expected: ${url}`)
})

describe("Unit test for publish event handler", function () {
    test("verifies successful stream", async () => {
        await lambdaFunction.handler(streamEvent, mockContext, mockCallback)

        const expectedAuthRequest = `grant_type=client_credentials&client_id=${config.clientId}&client_secret=${config.clientSecret}`
        const expectedFirstEventRequest = `{"eventType":"GRO_DEATH_NOTIFICATION","eventTime":"${firstEventRecord.eventTime}","id":"${firstEventRecord.hash}"}`
        const expectedSecondEventRequest = `{"eventType":"GRO_DEATH_NOTIFICATION","eventTime":"${secondEventRecord.eventTime}","id":"${secondEventRecord.hash}"}`

        expect(makeRequestSpy).toHaveBeenCalledTimes(3)
        expect(makeRequestSpy.mock.calls).toEqual([
            expect.arrayContaining([
                config.authUrl,
                {
                    headers: {
                        "Content-Length": expectedAuthRequest.length,
                        "Content-Type": "application/x-www-form-urlencoded",
                    },
                    method: "POST",
                },
                expectedAuthRequest,
            ]),
            expect.arrayContaining([
                config.gdxUrl,
                {
                    headers: {
                        Authorization: `Bearer ${mockAccessToken}`,
                        "Content-Length": expectedFirstEventRequest.length,
                        "Content-Type": "application/json; charset=utf-8",
                    },
                    method: "POST",
                    path: "/events",
                },
                expectedFirstEventRequest,
            ]),
            expect.arrayContaining([
                config.gdxUrl,
                {
                    headers: {
                        Authorization: `Bearer ${mockAccessToken}`,
                        "Content-Length": expectedSecondEventRequest.length,
                        "Content-Type": "application/json; charset=utf-8",
                    },
                    method: "POST",
                    path: "/events",
                },
                expectedSecondEventRequest,
            ]),
        ])
    })
})
