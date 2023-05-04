import { Handler } from "aws-lambda"

export interface LambdaFunction {
    name: string
    handler: Handler
}
