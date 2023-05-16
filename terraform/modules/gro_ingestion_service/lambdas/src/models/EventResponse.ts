import { EventRecord } from "./EventRecord"

export interface EventResponse<T> {
    statusCode: number
    payload?: T
}

export interface EnrichEventResponse extends EventResponse<EventRecord> {}

export interface DeleteEventResponse extends EventResponse<string> {}
