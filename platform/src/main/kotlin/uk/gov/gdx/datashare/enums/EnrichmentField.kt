package uk.gov.gdx.datashare.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class EnrichmentField(@JsonValue val jsonName: String) {
  SOURCE_ID("sourceId"),

  SEX("sex"),
  DATE_OF_BIRTH("dateOfBirth"),

  REGISTRATION_DATE("registrationDate"),
  FIRST_NAMES("firstNames"),
  LAST_NAME("lastName"),
  DATE_OF_DEATH("dateOfDeath"),
  BIRTH_PLACE("birthPlace"),
  DEATH_PLACE("deathPlace"),
  MAIDEN_NAME("maidenName"),
  OCCUPATION("occupation"),
  RETIRED("retired"),
  ADDRESS("address"),

  REGISTRATION_ID("registrationId"),
  EVENT_TIME("eventTime"),
  VERIFICATION_LEVEL("verificationLevel"),
  PARTIAL_MONTH_OF_DEATH("partialMonthOfDeath"),
  PARTIAL_YEAR_OF_DEATH("partialYearOfDeath"),
  FORENAMES("forenames"),
  SURNAME("surname"),
  MAIDEN_SURNAME("maidenSurname"),
  ADDRESS_LINE_1("addressLine1"),
  ADDRESS_LINE_2("addressLine2"),
  ADDRESS_LINE_3("addressLine3"),
  ADDRESS_LINE_4("addressLine4"),
  POSTCODE("postcode"),
}
