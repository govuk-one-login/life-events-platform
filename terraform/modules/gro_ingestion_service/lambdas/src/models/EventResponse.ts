import { EventRecord } from "./EventRecord"

export interface EventResponse<T> {
    statusCode: number
    payload?: T
}

export type EnrichEventResponse = EventResponse<EventRecord>

export type DeleteEventResponse = EventResponse<string>

export type InsertXmlResponse = EventResponse<string>
