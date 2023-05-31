import { PutObjectCommand, S3Client } from "@aws-sdk/client-s3"
import { Handler } from "aws-lambda"
import { randomUUID } from "crypto"
import { XMLBuilder } from "fast-xml-parser"

import { config } from "../helpers/config"
import { InsertXmlResponse } from "../models/EventResponse"
import { LambdaFunction } from "../models/LambdaFunction"

const s3Client = new S3Client({ apiVersion: "2012-08-10" })
const xmlBuilder = new XMLBuilder()

const titleOptions = ["Mr", "Mrs"]
const forenameOptions = ["Tester", ""]
const surnameOptions = ["SMITH", ""]
const dateOfBirthOptions = ["1912-02-29", ""]
const dateOfDeathOptions = ["2012-02-29", ""]
const genderOptions = ["1", "2"]
const addressOptions = [
    ["10 Test Street", "Test Town", "Test County"],
    ["", "Westminster", "London"],
    ["27 Brown Lane", "", "Somerset"],
    ["65 Link Road", "Southport", ""],
]
const postcodeOptions = ["PR8 1HY", ""]

const handler: Handler = async (event): Promise<InsertXmlResponse> => {
    const numberOfRecords =
        event["detail-type"] === "Scheduled Event" || !event["numberOfRecords"] ? 25 : event["numberOfRecords"]
    const fileKey = `${new Date().toISOString()}-fake-gro.xml`
    const logParams: { fileKey: string; error?: Error } = { fileKey: fileKey }

    try {
        const xml = generateXml(numberOfRecords)
        await uploadXml(xml, fileKey)
        console.log("Successfully generated GRO file", logParams)
        return {
            statusCode: 200,
            payload: fileKey,
        }
    } catch (err) {
        logParams.error = err
        console.error("Failed to generate GRO file", logParams)
        return {
            statusCode: 500,
        }
    }
}

const uploadXml = async (xml: string, fileKey: string) => {
    const putObjectCommand = new PutObjectCommand({
        Bucket: config.s3BucketArn,
        Key: fileKey,
        Body: xml,
    })
    await s3Client.send(putObjectCommand)
}

const generateXml = (numberOfRecords: number) => {
    const deathRecords = [...Array(numberOfRecords)].map(_ => createDeathRecord())
    return xmlBuilder.build({
        DeathRegistrationGroup: {
            DeathRegistration: deathRecords,
            RecordCount: numberOfRecords,
        },
    })
}

const createDeathRecord = () => {
    const title = getRandomElement(titleOptions)
    const forename = getRandomElement(forenameOptions)
    const surname = getRandomElement(surnameOptions)
    const date = new Date().toISOString().slice(0, 19)

    return {
        DeathRegistration: {
            RegistrationId: randomUUID(),
            RecordLockedDateTime: date,
            RecordUpdateDateTime: date,
            RecordUpdateReason: "",
            DeceasedName: {
                PersonNameTitle: title,
                PersonGivenName: forename,
                PersonFamilyName: surname,
                PersonNameSuffix: "",
            },
            DeceasedAliasName: {
                PersonNameTitle: title,
                PersonGivenName: forename,
                PersonFamilyName: surname,
                PersonNameSuffix: "",
            },
            DeceasedAliasNameType: "",
            DeceasedMaidenNameType: "",
            DeceasedGender: getRandomElement(genderOptions),
            DeceasedDeathDate: {
                PersonDeathDate: getRandomElement(dateOfDeathOptions),
                VerificationLevel: "03",
            },
            PartialMonthOfDeath: "",
            PartialYearOfDeath: "",
            QualifierText: "",
            FreeFormatDeathDate: "",
            DeceasedBirthDate: {
                PersonDeathDate: getRandomElement(dateOfBirthOptions),
                VerificationLevel: "",
            },
            PartialMonthOfBirth: "",
            PartialYearOfBirth: "",
            FreeFormatBirthDate: "",
            DeceasedAddress: {
                Flat: "",
                Building: "",
                Line: getRandomElement(addressOptions),
                Postcode: getRandomElement(postcodeOptions),
            },
        },
    }
}

const getRandomElement = <T>(array: T[]): T => array[Math.floor(Math.random() * array.length)]

const lambdaFunction: LambdaFunction = {
    name: "insertXml",
    handler: handler,
}
export default lambdaFunction
