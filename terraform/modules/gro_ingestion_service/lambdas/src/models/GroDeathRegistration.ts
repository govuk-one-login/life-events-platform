import hash from "object-hash"

import { EventRecord } from "./EventRecord"

export interface GroDeathRegistration {
    RegistrationID: string
    RegistrationType: string
    RecordLockedDateTime: string
    RecordUpdateDateTime: string
    RecordUpdateReason: string
    DeceasedName: {
        PersonNameTitle: string
        PersonGivenName: string
        PersonFamilyName: string
        PersonNameSuffix: string
    }
    DeceasedNameAlias: {
        PersonNameTitle: string
        PersonGivenName: string
        PersonFamilyName: string
        PersonNameSuffix: string
    }
    DeceasedAliasNameType: string
    DeceasedMaidenName: string
    DeceasedGender: number
    DeceasedDeathDate: {
        PersonDeathDate: string
        VerificationLevel: number
    }
    PartialMonthOfDeath: string
    PartialYearOfDeath: string
    QualifierText: string
    FreeFormatDeathDate: string
    DeceasedBirthDate: {
        PersonBirthDate: string
        VerificationLevel: string
    }
    PartialMonthOfBirth: string
    PartialYearOfBirth: string
    FreeFormatBirthDate: string
    DeceasedAddress: {
        Flat: string
        Building: string
        Line: string[]
        Postcode: string
    }
}

export const mapToEventRecord = (deathRecord: GroDeathRegistration): EventRecord => {
    return {
        hash: hash(deathRecord),
        RegistrationId: deathRecord.RegistrationID,
        EventTime: deathRecord.RecordUpdateDateTime,
        DateOfDeath: deathRecord.DeceasedDeathDate.PersonDeathDate,
        VerificationLevel: deathRecord.DeceasedDeathDate.VerificationLevel.toString(),
        PartialMonthOfDeath: deathRecord.PartialMonthOfDeath,
        PartialYearOfDeath: deathRecord.PartialYearOfDeath,
        FirstForename: deathRecord.DeceasedName.PersonGivenName,
        Surname: deathRecord.DeceasedName.PersonFamilyName,
        MaidenSurname: deathRecord.DeceasedMaidenName,
        Sex: deathRecord.DeceasedGender === 1 ? "M" : "F",
        DateOfBirth: deathRecord.DeceasedBirthDate.PersonBirthDate,
        AddressLine1: deathRecord.DeceasedAddress.Flat,
        AddressLine2: deathRecord.DeceasedAddress.Building,
        AddressLine3: deathRecord.DeceasedAddress.Building,
        AddressLine4: deathRecord.DeceasedAddress.Line.join("\n"),
        Postcode: deathRecord.DeceasedAddress.Postcode,
    }
}
