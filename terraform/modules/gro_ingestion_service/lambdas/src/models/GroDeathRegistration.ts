import { AttributeValue } from "@aws-sdk/client-dynamodb"

export interface GroDeathRegistration {
    RegistrationId: string,
    RegistrationType: string,
    RecordLockedDateTime: string,
    RecordUpdateDateTime: string,
    RecordUpdateReason: string,
    DeceasedName: {
        PersonNameTitle: string,
        PersonGivenName: string,
        PersonFamilyName: string,
        PersonNameSuffix: string,
    },
    DeceasedNameAlias: {
        PersonNameTitle: string,
        PersonGivenName: string,
        PersonFamilyName: string,
        PersonNameSuffix: string,
    },
    DeceasedAliasNameType: string,
    DeceasedMaidenName: string,
    DeceasedGender: string,
    DeceasedDeathDate: {
        PersonDeathDate: string,
        VerificationLevel: string,
    },
    PartialMonthOfDeath: string,
    PartialYearOfDeath: string,
    QualifierText: string,
    FreeFormatDeathDate: string,
    DeceasedBirthDate: {
        PersonBirthDate: string,
        VerificationLevel: string,
    },
    PartialMonthOfBirth: string,
    PartialYearOfBirth: string,
    FreeFormatBirthDate: string,
    DeceasedAddress: {
        Flat: string,
        Building: string,
        Line: string[],
        Postcode: string,
    },
}


export const mapToDynamoDbItem = (deathRecord: GroDeathRegistration, hash: string): { [key: string]: AttributeValue } => {
    return {
        hash: { S: hash },
        RegistrationId: { S: deathRecord.RegistrationId },
        EventTime: { S: deathRecord.RecordUpdateDateTime },
        DateOfDeath: { S: deathRecord.DeceasedDeathDate.PersonDeathDate },
        VerificationLevel: { S: deathRecord.DeceasedDeathDate.VerificationLevel },
        PartialMonthOfDeath: { S: deathRecord.PartialMonthOfDeath },
        PartialYearOfDeath: { S: deathRecord.PartialYearOfDeath },
        FirstForename: { S: deathRecord.DeceasedName.PersonGivenName },
        Surname: { S: deathRecord.DeceasedName.PersonFamilyName },
        MaidenSurname: { S: deathRecord.DeceasedMaidenName },
        Sex: { S: deathRecord.DeceasedGender === "1" ? "M" : "F" },
        DateOfBirth: { S: deathRecord.DeceasedBirthDate.PersonBirthDate },
        AddressLine1: { S: deathRecord.DeceasedAddress.Flat },
        AddressLine2: { S: deathRecord.DeceasedAddress.Building },
        AddressLine3: { S: deathRecord.DeceasedAddress.Building },
        AddressLine4: { S: deathRecord.DeceasedAddress.Line.join("\n") },
        Postcode: { S: deathRecord.DeceasedAddress.Postcode },
    }
}
