import { GroDeathRegistration } from "../../src/models/GroDeathRegistration"

export const groDeathRegistration: GroDeathRegistration = {
    RegistrationID: "RegistrationID",
    RegistrationType: "RegistrationType",
    RecordLockedDateTime: "RecordLockedDateTime",
    RecordUpdateDateTime: "RecordUpdateDateTime",
    RecordUpdateReason: "RecordUpdateReason",
    DeceasedName: {
        PersonNameTitle: "PersonNameTitle",
        PersonGivenName: "PersonGivenName",
        PersonFamilyName: "PersonFamilyName",
        PersonNameSuffix: "PersonNameSuffix",
    },
    DeceasedNameAlias: {
        PersonNameTitle: "PersonNameTitle",
        PersonGivenName: "PersonGivenName",
        PersonFamilyName: "PersonFamilyName",
        PersonNameSuffix: "PersonNameSuffix",
    },
    DeceasedAliasNameType: "DeceasedAliasNameType",
    DeceasedMaidenName: "DeceasedMaidenName",
    DeceasedGender: 1,
    DeceasedDeathDate: {
        PersonDeathDate: "PersonDeathDate",
        VerificationLevel: 1,
    },
    PartialMonthOfDeath: "1",
    PartialYearOfDeath: "1",
    QualifierText: "QualifierText",
    FreeFormatDeathDate: "FreeFormatDeathDate",
    DeceasedBirthDate: {
        PersonBirthDate: "PersonBirthDate",
        VerificationLevel: "VerificationLevel",
    },
    PartialMonthOfBirth: "PartialMonthOfBirth",
    PartialYearOfBirth: "PartialYearOfBirth",
    FreeFormatBirthDate: "FreeFormatBirthDate",
    DeceasedAddress: {
        Flat: "Flat",
        Building: "Building",
        Line: ["Line"],
        Postcode: "Postcode",
    },
}
