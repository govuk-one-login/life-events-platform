import { Handler } from "aws-lambda"

import { functions } from "./functions"

export const handler: Handler = async (event, context, callback) => {
    const functionName = process.env.FUNCTION_NAME ?? "-"

    const lambdaFunction = functions.find(f => f.name === functionName)
    if (!lambdaFunction) {
        throw Error(`Lambda function ${functionName} not found`)
    }

    return lambdaFunction.handler(event, context, callback)
}
