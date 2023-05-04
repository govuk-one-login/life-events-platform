import { Handler } from "aws-lambda"

import deleteEvent from "./functions/deleteEvent"
import deleteXml from "./functions/deleteXml"
import enrichEvent from "./functions/enrichEvent"
import mapXml from "./functions/mapXml"
import publishEvent from "./functions/publishEvent"
import splitXml from "./functions/splitXml"
import { LambdaFunction } from "./models/LambdaFunction"

export const handler: Handler = async (event, context, callback) => {
    const functionName = process.env.FUNCTION_NAME ?? "-"
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const functions: LambdaFunction[] = [
        deleteEvent,
        deleteXml,
        enrichEvent,
        mapXml,
        publishEvent,
        splitXml,
    ]

    const lambdaFunction = functions.find(f => f.name === `${functionName}`)
    if (!lambdaFunction) {
        throw Error(`Lambda function ${functionName} not found`)
    }

    return lambdaFunction.handler(event, context, callback)
}
