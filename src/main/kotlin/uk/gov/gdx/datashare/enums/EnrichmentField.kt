package uk.gov.gdx.datashare.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class EnrichmentField(@JsonValue val jsonName: String) {
  SOURCE_ID("sourceId"),

  LAST_NAME("lastName"),
  SEX("sex"),
  DATE_OF_BIRTH("dateOfBirth"),

  REGISTRATION_DATE("registrationDate"),
  FIRST_NAMES("firstNames"),
  DATE_OF_DEATH("dateOfDeath"),
  BIRTH_PLACE("birthPlace"),
  DEATH_PLACE("deathPlace"),
  MAIDEN_NAME("maidenName"),
  OCCUPATION("occupation"),
  RETIRED("retired"),
  ADDRESS("address"),

  PRISONER_NUMBER("prisonerNumber"),
  FIRST_NAME("firstName"),
  MIDDLE_NAMES("middleNames"),
  GENDER("gender"),
}
