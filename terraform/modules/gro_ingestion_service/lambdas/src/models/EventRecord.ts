import { AttributeValue } from "aws-lambda/trigger/dynamodb-stream"

export interface EventRecord {
    hash: string

    RegistrationId: string
    EventTime: string
    DateOfDeath: string
    VerificationLevel: string
    PartialMonthOfDeath: string
    PartialYearOfDeath: string

    FirstForename: string
    Surname: string
    MaidenSurname: string
    Sex: string
    DateOfBirth: string
    AddressLine1: string
    AddressLine2: string
    AddressLine3: string
    AddressLine4: string
    Postcode: string
}

export const mapToEventRecord = (image: { [key: string]: AttributeValue }): EventRecord => {
    return {
        hash: image["hash"].S ?? "",
        RegistrationId: image["RegistrationId"].S ?? "",
        EventTime: image["EventTime"].S ?? "",
        DateOfDeath: image["DateOfDeath"].S ?? "",
        VerificationLevel: image["VerificationLevel"].S ?? "",
        PartialMonthOfDeath: image["PartialMonthOfDeath"].S ?? "",
        PartialYearOfDeath: image["PartialYearOfDeath"].S ?? "",
        FirstForename: image["FirstForename"].S ?? "",
        Surname: image["Surname"].S ?? "",
        MaidenSurname: image["MaidenSurname"].S ?? "",
        Sex: image["Sex"].S ?? "",
        DateOfBirth: image["DateOfBirth"].S ?? "",
        AddressLine1: image["AddressLine1"].S ?? "",
        AddressLine2: image["AddressLine2"].S ?? "",
        AddressLine3: image["AddressLine3"].S ?? "",
        AddressLine4: image["AddressLine4"].S ?? "",
        Postcode: image["Postcode"].S ?? "",
    }
}
