package uk.gov.gdx.datashare.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class DeathNotificationField(@JsonValue val jsonName: String) {
  REGISTRATION_DATE("registrationDate"),
  FIRST_NAMES("firstNames"),
  LAST_NAME("lastName"),
  SEX("sex"),
  DATE_OF_DEATH("dateOfDeath"),
  DATE_OF_BIRTH("dateOfBirth"),
  BIRTH_PLACE("birthPlace"),
  DEATH_PLACE("deathPlace"),
  MAIDEN_NAME("maidenName"),
  OCCUPATION("occupation"),
  RETIRED("retired"),
  ADDRESS("address"),
}
