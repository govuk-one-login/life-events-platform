import { EventRecord } from "./EventRecord"

export interface EnrichEventResponse {
    statusCode: number
    event?: EventRecord
}
