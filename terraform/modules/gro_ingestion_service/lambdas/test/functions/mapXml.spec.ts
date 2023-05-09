import { marshall } from "@aws-sdk/util-dynamodb"
import { describe, expect } from "@jest/globals"

import lambdaFunction from "../../src/functions/mapXml"
import { config } from "../../src/helpers/config"
import { mapToEventRecord } from "../../src/models/GroDeathRegistration"
import { dynamoDbSendFn } from "../__mocks__/@aws-sdk/client-dynamodb"
import { mockCallback, mockContext } from "../const/aws-lambda"
import { groDeathRegistration } from "../const/groDeathRegistration"

jest.mock("../../src/helpers/config")

describe("Unit test for map xml handler", function () {
    test("verifies successful response", async () => {
        dynamoDbSendFn.mockReturnValueOnce(Promise.resolve())

        await lambdaFunction.handler(groDeathRegistration, mockContext, mockCallback)

        const record = mapToEventRecord(groDeathRegistration)
        const dbItem = marshall(record, { convertEmptyValues: false, removeUndefinedValues: false })

        expect(dynamoDbSendFn).toHaveBeenCalledWith(
            expect.objectContaining({
                input: {
                    Item: dbItem,
                    TableName: config.tableName,
                },
            }),
        )
    })
})
