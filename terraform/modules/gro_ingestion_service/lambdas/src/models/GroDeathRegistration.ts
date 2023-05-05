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
        registrationId: deathRecord.RegistrationID,
        eventTime: deathRecord.RecordUpdateDateTime,
        dateOfDeath: deathRecord.DeceasedDeathDate.PersonDeathDate,
        verificationLevel: deathRecord.DeceasedDeathDate.VerificationLevel.toString(),
        partialMonthOfDeath: deathRecord.PartialMonthOfDeath,
        partialYearOfDeath: deathRecord.PartialYearOfDeath,
        forenames: deathRecord.DeceasedName.PersonGivenName,
        surname: deathRecord.DeceasedName.PersonFamilyName,
        maidenSurname: deathRecord.DeceasedMaidenName,
        sex: deathRecord.DeceasedGender === 1 ? "M" : "F",
        dateOfBirth: deathRecord.DeceasedBirthDate.PersonBirthDate,
        addressLine1: deathRecord.DeceasedAddress.Flat,
        addressLine2: deathRecord.DeceasedAddress.Building,
        addressLine3: deathRecord.DeceasedAddress.Building,
        addressLine4: deathRecord.DeceasedAddress.Line.join("\n"),
        postcode: deathRecord.DeceasedAddress.Postcode,
    }
}
