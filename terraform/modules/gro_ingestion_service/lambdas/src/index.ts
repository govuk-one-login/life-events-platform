import { Handler } from "aws-lambda";

export const handler: Handler = async (event, context, callback) => {
    const functionName = process.env.FUNCTION_NAME ?? `-`;
    const lambdaFunction: Handler = require(`./functions/${functionName}.function`).handler;
    return lambdaFunction(event, context, callback);
};
