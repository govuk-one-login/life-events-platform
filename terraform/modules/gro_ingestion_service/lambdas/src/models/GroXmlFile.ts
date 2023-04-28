import { GroDeathRegistration } from "./GroDeathRegistration"

export interface GroXmlFile {
    deathRegistrationGroup: GroDeathRegistration[],
    recordCount: number,
}
