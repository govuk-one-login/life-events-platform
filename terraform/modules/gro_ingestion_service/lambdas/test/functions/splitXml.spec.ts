import { describe, expect } from "@jest/globals"
import { S3ObjectCreatedNotificationEvent } from "aws-lambda"

import lambdaFunction from "../../src/functions/splitXml"
import { s3SendFn } from "../__mocks__/@aws-sdk/client-s3"
import { mockCallback, mockContext } from "../const/aws-lambda"

const event: S3ObjectCreatedNotificationEvent = {
    detail: {
        bucket: {
            name: "test_bucket",
        },
        object: {
            key: "test_object_key",
        },
    },
} as S3ObjectCreatedNotificationEvent

describe("Unit test for split xml handler", function () {
    beforeEach(() => {
        s3SendFn.mockReset()
    })

    test("verifies successful response", async () => {
        s3SendFn.mockReturnValueOnce(
            Promise.resolve({
                Body: {
                    transformToString: () =>
                        `<DeathRegistrationGroup>
    <DeathRegistration>
        <RegistrationID>1</RegistrationID>
    </DeathRegistration>
    <DeathRegistration>
        <RegistrationID>2</RegistrationID>
    </DeathRegistration>
</DeathRegistrationGroup>`.trim(),
                },
            }),
        )

        const result = await lambdaFunction.handler(event, mockContext, mockCallback)

        expect(s3SendFn).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Bucket: event.detail.bucket.name,
                    Key: event.detail.object.key,
                },
            }),
        )
        expect(result).toStrictEqual({
            bucket: event.detail.bucket.name,
            key: event.detail.object.key,
            deathRegistrations: [
                {
                    RegistrationID: 1,
                },
                {
                    RegistrationID: 2,
                },
            ],
        })
    })

    test("verifies not found if no object found", async () => {
        s3SendFn.mockReturnValueOnce(
            Promise.resolve({
                Body: null,
            }),
        )

        const result = await lambdaFunction.handler(event, mockContext, mockCallback)

        expect(s3SendFn).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Bucket: event.detail.bucket.name,
                    Key: event.detail.object.key,
                },
            }),
        )
        expect(result).toStrictEqual({
            statusCode: 404,
        })
    })
})
