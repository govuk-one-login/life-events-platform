import { LambdaFunction } from "../models/LambdaFunction"
import deleteEvent from "./deleteEvent"
import deleteXml from "./deleteXml"
import enrichEvent from "./enrichEvent"
import mapXml from "./mapXml"
import publishEvent from "./publishEvent"
import splitXml from "./splitXml"

export const functions: LambdaFunction[] = [deleteEvent, deleteXml, enrichEvent, mapXml, publishEvent, splitXml]
