package uk.gov.gdx.datashare.enums

enum class EventType {
  GRO_DEATH_NOTIFICATION,
  DEATH_NOTIFICATION,
  LIFE_EVENT,
  TEST_EVENT,
}

val EventTypeEnrichmentFieldsRelationship = mapOf(
  EventType.GRO_DEATH_NOTIFICATION to listOf<EnrichmentField>(
    EnrichmentField.SOURCE_ID,
    EnrichmentField.SEX,
    EnrichmentField.DATE_OF_BIRTH,
    EnrichmentField.DATE_OF_DEATH,
    EnrichmentField.REGISTRATION_ID,
    EnrichmentField.EVENT_TIME,
    EnrichmentField.VERIFICATION_LEVEL,
    EnrichmentField.PARTIAL_MONTH_OF_DEATH,
    EnrichmentField.PARTIAL_YEAR_OF_DEATH,
    EnrichmentField.FORENAMES,
    EnrichmentField.SURNAME,
    EnrichmentField.MAIDEN_SURNAME,
    EnrichmentField.ADDRESS_LINE_1,
    EnrichmentField.ADDRESS_LINE_2,
    EnrichmentField.ADDRESS_LINE_3,
    EnrichmentField.ADDRESS_LINE_4,
    EnrichmentField.POSTCODE,
  ),
  EventType.DEATH_NOTIFICATION to listOf<EnrichmentField>(
    EnrichmentField.SOURCE_ID,
    EnrichmentField.SEX,
    EnrichmentField.DATE_OF_BIRTH,
    EnrichmentField.REGISTRATION_DATE,
    EnrichmentField.FIRST_NAMES,
    EnrichmentField.LAST_NAME,
    EnrichmentField.DATE_OF_DEATH,
    EnrichmentField.BIRTH_PLACE,
    EnrichmentField.DEATH_PLACE,
    EnrichmentField.MAIDEN_NAME,
    EnrichmentField.OCCUPATION,
    EnrichmentField.RETIRED,
    EnrichmentField.ADDRESS,
  ),
  EventType.TEST_EVENT to listOf<EnrichmentField>(
    EnrichmentField.SOURCE_ID,
    EnrichmentField.EVENT_TIME
  )
)
