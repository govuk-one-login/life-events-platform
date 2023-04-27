import {Handler} from "aws-lambda"
import {DocumentClient} from "aws-sdk/clients/dynamodb"
import {mapToEventRecord} from "../models/EventRecord";
import {EnrichEventRequest} from "../models/EnrichEventRequest";
import {EnrichEventResponse} from "../models/EnrichEventResponse";

const tableName = process.env.TABLE_NAME ?? ""

const dynamo = new DocumentClient({apiVersion: '2012-08-10'})

export const handler: Handler = async (event: EnrichEventRequest, context, callback): Promise<EnrichEventResponse> => {

    const params: DocumentClient.GetItemInput = {
        Key: {
            hash: event.id
        },
        TableName: tableName
    };

    const result = await dynamo.get(params).promise()

    if (!result.Item) {
        console.log(`Record with hash ${event.id} not found`)
        return {
            statusCode: 404
        }
    }

    return {
        statusCode: 200,
        event: mapToEventRecord(result.Item)
    }

}
