import { Handler, S3Event } from "aws-lambda";

export const handler: Handler = async (event: S3Event, context, callback) => {
    console.log('EVENT: \n' + JSON.stringify(event, null, 2));
    return context.logStreamName;
};
