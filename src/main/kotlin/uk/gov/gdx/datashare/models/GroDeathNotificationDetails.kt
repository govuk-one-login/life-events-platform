package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.format.annotation.DateTimeFormat
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.GroSex
import uk.gov.gdx.datashare.extensions.writeNullableStringField
import java.time.LocalDate

@Schema(description = "GRO Death notification")
@JsonSerialize(using = GroDeathNotificationDetailsSerializer::class)
data class GroDeathNotificationDetails(
  val enrichmentFields: List<EnrichmentField>,

  @Schema(description = "Registration ID of death record", example = "123456789")
  val registrationId: String? = null,

  @Schema(description = "Date the death was registered", example = "2022-01-05", type = "date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  val eventTime: LocalDate? = null,

  @Schema(description = "Date the person died", example = "2021-12-31", type = "date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  val dateOfDeath: LocalDate? = null,

  @Schema(description = "Verification level of this data", example = "1")
  val verificationLevel: String? = null,

  @Schema(description = "Month the person died", example = "1")
  val partialMonthOfDeath: String? = null,

  @Schema(description = "Year the person died", example = "2021")
  val partialYearOfDeath: String? = null,

  @Schema(description = "Forenames of the deceased", example = "Bob Burt")
  val forenames: String? = null,

  @Schema(description = "Surname of the deceased", example = "Smith")
  val surname: String? = null,

  @Schema(description = "Maiden name of the deceased", example = "Jane")
  val maidenSurname: String? = null,

  @Schema(description = "Sex of the deceased", example = "F")
  val sex: GroSex? = null,

  @Schema(description = "Date the deceased was born", example = "2001-12-31", type = "date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  val dateOfBirth: LocalDate? = null,

  @Schema(
    description = "The deceased's address line 1",
    example = "888 Death House",
  )
  val addressLine1: String? = null,

  @Schema(
    description = "The deceased's address line 2",
    example = "8 Death lane",
  )
  val addressLine2: String? = null,

  @Schema(
    description = "The deceased's address line 3",
    example = "Deadington",
  )
  val addressLine3: String? = null,

  @Schema(
    description = "The deceased's address line 4",
    example = "Deadshire",
  )
  val addressLine4: String? = null,

  @Schema(
    description = "The deceased's postcode",
    example = "XX1 1XX",
  )
  val postcode: String? = null,
) : EventDetails {
  companion object : EventDetailsCompanionObject<GroDeathNotificationDetails, GroDeathRecord> {
    override fun from(enrichmentFields: List<EnrichmentField>, data: GroDeathRecord): GroDeathNotificationDetails {
      with(data) {
        return GroDeathNotificationDetails(
          enrichmentFields = enrichmentFields,
          registrationId = if (enrichmentFields.contains(EnrichmentField.REGISTRATION_ID)) registrationId else null,
          eventTime = if (enrichmentFields.contains(EnrichmentField.EVENT_TIME)) eventTime else null,
          verificationLevel = if (enrichmentFields.contains(EnrichmentField.VERIFICATION_LEVEL)) verificationLevel else null,
          dateOfDeath = if (enrichmentFields.contains(EnrichmentField.DATE_OF_DEATH)) dateOfDeath else null,
          partialMonthOfDeath = if (enrichmentFields.contains(EnrichmentField.PARTIAL_MONTH_OF_DEATH)) partialMonthOfDeath else null,
          partialYearOfDeath = if (enrichmentFields.contains(EnrichmentField.PARTIAL_YEAR_OF_DEATH)) partialYearOfDeath else null,
          forenames = if (enrichmentFields.contains(EnrichmentField.FORENAMES)) forenames else null,
          surname = if (enrichmentFields.contains(EnrichmentField.SURNAME)) surname else null,
          maidenSurname = if (enrichmentFields.contains(EnrichmentField.MAIDEN_SURNAME)) maidenSurname else null,
          sex = if (enrichmentFields.contains(EnrichmentField.SEX)) sex else null,
          dateOfBirth = if (enrichmentFields.contains(EnrichmentField.DATE_OF_BIRTH)) dateOfBirth else null,
          addressLine1 = if (enrichmentFields.contains(EnrichmentField.ADDRESS_LINE_1)) addressLine1 else null,
          addressLine2 = if (enrichmentFields.contains(EnrichmentField.ADDRESS_LINE_2)) addressLine2 else null,
          addressLine3 = if (enrichmentFields.contains(EnrichmentField.ADDRESS_LINE_3)) addressLine3 else null,
          addressLine4 = if (enrichmentFields.contains(EnrichmentField.ADDRESS_LINE_4)) addressLine4 else null,
          postcode = if (enrichmentFields.contains(EnrichmentField.POSTCODE)) postcode else null,
        )
      }
    }
  }
}

class GroDeathNotificationDetailsSerializer :
  StdSerializer<GroDeathNotificationDetails>(GroDeathNotificationDetails::class.java) {
  override fun serialize(value: GroDeathNotificationDetails?, gen: JsonGenerator?, provider: SerializerProvider?) {
    gen?.let {
      gen.writeStartObject()
      value?.run {
        if (enrichmentFields.contains(EnrichmentField.REGISTRATION_ID)) {
          gen.writeNullableStringField(EnrichmentField.REGISTRATION_ID.jsonName, registrationId)
        }
        if (enrichmentFields.contains(EnrichmentField.EVENT_TIME)) {
          gen.writeNullableStringField(EnrichmentField.EVENT_TIME.jsonName, eventTime?.toString())
        }
        if (enrichmentFields.contains(EnrichmentField.VERIFICATION_LEVEL)) {
          gen.writeNullableStringField(EnrichmentField.VERIFICATION_LEVEL.jsonName, verificationLevel)
        }
        if (enrichmentFields.contains(EnrichmentField.DATE_OF_DEATH)) {
          gen.writeNullableStringField(EnrichmentField.DATE_OF_DEATH.jsonName, dateOfDeath?.toString())
        }
        if (enrichmentFields.contains(EnrichmentField.PARTIAL_MONTH_OF_DEATH)) {
          gen.writeNullableStringField(EnrichmentField.PARTIAL_MONTH_OF_DEATH.jsonName, partialMonthOfDeath)
        }
        if (enrichmentFields.contains(EnrichmentField.PARTIAL_YEAR_OF_DEATH)) {
          gen.writeNullableStringField(EnrichmentField.PARTIAL_YEAR_OF_DEATH.jsonName, partialYearOfDeath)
        }
        if (enrichmentFields.contains(EnrichmentField.FORENAMES)) {
          gen.writeNullableStringField(EnrichmentField.FORENAMES.jsonName, forenames)
        }
        if (enrichmentFields.contains(EnrichmentField.SURNAME)) {
          gen.writeNullableStringField(EnrichmentField.SURNAME.jsonName, surname)
        }
        if (enrichmentFields.contains(EnrichmentField.MAIDEN_SURNAME)) {
          gen.writeNullableStringField(EnrichmentField.MAIDEN_SURNAME.jsonName, maidenSurname)
        }
        if (enrichmentFields.contains(EnrichmentField.SEX)) {
          gen.writeNullableStringField(EnrichmentField.SEX.jsonName, sex?.jsonName)
        }
        if (enrichmentFields.contains(EnrichmentField.DATE_OF_BIRTH)) {
          gen.writeNullableStringField(EnrichmentField.DATE_OF_BIRTH.jsonName, dateOfBirth?.toString())
        }
        if (enrichmentFields.contains(EnrichmentField.ADDRESS_LINE_1)) {
          gen.writeNullableStringField(EnrichmentField.ADDRESS_LINE_1.jsonName, addressLine1)
        }
        if (enrichmentFields.contains(EnrichmentField.ADDRESS_LINE_2)) {
          gen.writeNullableStringField(EnrichmentField.ADDRESS_LINE_2.jsonName, addressLine2)
        }
        if (enrichmentFields.contains(EnrichmentField.ADDRESS_LINE_3)) {
          gen.writeNullableStringField(EnrichmentField.ADDRESS_LINE_3.jsonName, addressLine3)
        }
        if (enrichmentFields.contains(EnrichmentField.ADDRESS_LINE_4)) {
          gen.writeNullableStringField(EnrichmentField.ADDRESS_LINE_4.jsonName, addressLine4)
        }
        if (enrichmentFields.contains(EnrichmentField.POSTCODE)) {
          gen.writeNullableStringField(EnrichmentField.POSTCODE.jsonName, postcode)
        }
      }
      gen.writeEndObject()
    }
  }
}
