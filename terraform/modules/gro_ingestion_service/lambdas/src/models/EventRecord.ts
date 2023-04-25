import {AttributeValue} from "aws-lambda/trigger/dynamodb-stream";

export interface EventRecord {
    hash: String

    RegistrationId: String
    EventTime: String
    DateOfDeath: String
    VerificationLevel: String
    PartialMonthOfDeath: String
    PartialYearOfDeath: String

    FirstForename: String
    Surname: String
    MaidenSurname: String
    Sex: String
    DateOfBirth: String
    AddressLine1: String
    AddressLine2: String
    AddressLine3: String
    AddressLine4: String
    Postcode: String
}

export const mapToEventRecord = (image: { [key: string]: AttributeValue }): EventRecord => {
    return {
        hash: image["hash"].S!,
        RegistrationId: image["RegistrationId"].S!,
        EventTime: image["EventTime"].S!,
        DateOfDeath: image["DateOfDeath"].S!,
        VerificationLevel: image["VerificationLevel"].S!,
        PartialMonthOfDeath: image["PartialMonthOfDeath"].S!,
        PartialYearOfDeath: image["PartialYearOfDeath"].S!,
        FirstForename: image["FirstForename"].S!,
        Surname: image["Surname"].S!,
        MaidenSurname: image["MaidenSurname"].S!,
        Sex: image["Sex"].S!,
        DateOfBirth: image["DateOfBirth"].S!,
        AddressLine1: image["AddressLine1"].S!,
        AddressLine2: image["AddressLine2"].S!,
        AddressLine3: image["AddressLine3"].S!,
        AddressLine4: image["AddressLine4"].S!,
        Postcode: image["Postcode"].S!,
    }
}
