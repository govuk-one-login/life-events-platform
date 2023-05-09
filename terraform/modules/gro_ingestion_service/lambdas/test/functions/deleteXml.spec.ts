import { describe, expect } from "@jest/globals"

import lambdaFunction from "../../src/functions/deleteXml"
import { BucketObjectDetails } from "../../src/models/BucketObjectDetails"
import { s3SendFn } from "../__mocks__/@aws-sdk/client-s3"
import { mockCallback, mockContext } from "../const/aws-lambda"

describe("Unit test for delete xml handler", function () {
    test("verifies successful response", async () => {
        const bucketObjectDetails: BucketObjectDetails = {
            bucket: "test_bucket_name",
            key: "test_key_id"
        }

        s3SendFn.mockReturnValueOnce(Promise.resolve())

        await lambdaFunction.handler(bucketObjectDetails, mockContext, mockCallback)

        expect(s3SendFn).toHaveBeenCalledWith(expect.objectContaining({
            input: {
                Bucket: bucketObjectDetails.bucket,
                Key: bucketObjectDetails.key,
            }
        }))
    })
})
