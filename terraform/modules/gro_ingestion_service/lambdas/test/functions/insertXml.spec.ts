import { describe, expect } from "@jest/globals"
import { DeleteEventResponse, InsertXmlResponse } from "../../src/models/EventResponse"
import lambdaFunction from "../../src/functions/insertXml"
import { eventRequest } from "../const/eventRequest"
import { mockCallback, mockContext } from "../const/aws-lambda"
import { config } from "../../src/helpers/config"
import { s3SendFn } from "../__mocks__/@aws-sdk/client-s3"

jest.mock("../../src/helpers/config")

describe("Unit test for delete xml handler", function () {
    beforeEach(() => {
        s3SendFn.mockReset()
    })

    test("verifies successful response", async () => {
        s3SendFn.mockReturnValueOnce(Promise.resolve())

        const result: InsertXmlResponse = await lambdaFunction.handler(eventRequest, mockContext, mockCallback)

        expect(s3SendFn).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Key: expect.stringContaining("fake-gro.xml"),
                    Bucket: config.s3BucketArn,
                    Body: expect.stringContaining("<DeathRegistrationGroup>"),
                },
            }),
        )
        expect(result).toEqual({
            payload: expect.stringContaining("fake-gro.xml"),
            statusCode: 200,
        })
    })

    test("verifies delete failure response", async () => {
        s3SendFn.mockImplementation(() => {
            throw new Error()
        })

        const result: DeleteEventResponse = await lambdaFunction.handler(eventRequest, mockContext, mockCallback)

        expect(s3SendFn).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Key: expect.stringContaining("fake-gro.xml"),
                    Bucket: config.s3BucketArn,
                    Body: expect.stringContaining("<DeathRegistrationGroup>"),
                },
            }),
        )
        expect(result).toEqual({
            statusCode: 500,
        })
    })
})
