import { eventRequest } from "./eventRequest"

export const dbItem = {
    hash: {
        S: eventRequest.id,
    },
    RegistrationId: {
        S: "111",
    },
    EventTime: {
        S: "2023-01-11",
    },
    DateOfDeath: {
        S: "2023-01-01",
    },
    FirstForename: {
        S: "Forename",
    },
    Surname: {
        S: "Surname",
    },
    Sex: {
        S: "Male",
    },
    MaidenSurname: {
        S: null,
    },
    DateOfBirth: {
        S: "1945-01-02",
    },
    AddressLine1: {
        S: "1 Death Lane",
    },
    AddressLine2: {
        S: null,
    },
    AddressLine3: {
        S: null,
    },
    AddressLine4: {
        S: null,
    },
    PartialMonthOfDeath: {
        S: "Jan",
    },
    PartialYearOfDeath: {
        S: "2023",
    },
    Postcode: {
        S: "S11 9GH",
    },
    VerificationLevel: {
        S: null,
    },
}
