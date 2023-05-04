import { Handler } from "aws-lambda"

import { handler as deleteEventHandler } from "./functions/deleteEvent.function"
import { handler as deleteXmlHandler } from "./functions/deleteXml.function"
import { handler as enrichEventHandler } from "./functions/enrichEvent.function"
import { handler as mapXmlHandler } from "./functions/mapXml.function"
import { handler as publishEventHandler } from "./functions/publishEvent.function"
import { handler as splitXmlHandler } from "./functions/splitXml.function"

export const handler: Handler = async (event, context, callback) => {
    const functionName = process.env.FUNCTION_NAME ?? "-"
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const functions = [
        deleteEventHandler,
        deleteXmlHandler,
        enrichEventHandler,
        mapXmlHandler,
        publishEventHandler,
        splitXmlHandler,
    ]

    const lambdaFunction = functions.find(f => f.name === `${functionName}Handler`)
    if (!lambdaFunction) {
        throw Error(`Lambda function ${functionName} not found`)
    }

    return lambdaFunction(event, context, callback)
}
