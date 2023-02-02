package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.format.annotation.DateTimeFormat
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.Sex
import uk.gov.gdx.datashare.extensions.writeNullableBooleanField
import uk.gov.gdx.datashare.extensions.writeNullableStringField
import java.time.LocalDate

@Schema(description = "Death notification")
@JsonSerialize(using = DeathNotificationDetailsSerializer::class)
data class DeathNotificationDetails(
  val enrichmentFields: List<EnrichmentField>,
  @Schema(description = "Date the death was registered", required = true, example = "2022-01-05", type = "date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  val registrationDate: LocalDate? = null,

  @Schema(description = "Forenames of the deceased", required = true, example = "Bob Burt")
  val firstNames: String? = null,

  @Schema(description = "Surname of the deceased", required = true, example = "Smith")
  val lastName: String? = null,

  @Schema(description = "Sex of the deceased", required = true, example = "Female")
  val sex: Sex? = null,

  @Schema(description = "Date the person died", required = true, example = "2021-12-31", type = "date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  val dateOfDeath: LocalDate? = null,

  @Schema(description = "Maiden name of the deceased", required = false, example = "Jane")
  val maidenName: String? = null,

  @Schema(description = "Date the deceased was born", required = false, example = "2001-12-31", type = "date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  val dateOfBirth: LocalDate? = null,

  @Schema(
    description = "The deceased's address",
    required = false,
    example = "888 Death House, 8 Death lane, Deadington, Deadshire",
  )
  val address: String? = null,

  @Schema(description = "The birthplace of the deceased", required = false, example = "Sheffield")
  val birthPlace: String? = null,

  @Schema(description = "The place the person died", required = false, example = "Swansea")
  val deathPlace: String? = null,

  @Schema(description = "The occupation of the deceased", required = false, example = "Doctor")
  val occupation: String? = null,

  @Schema(description = "Whether the deceased was retired", required = false, example = "false")
  val retired: Boolean? = null,
) {
  companion object {
    fun fromLevDeathRecord(
      enrichmentFields: List<EnrichmentField>,
      levDeathRecord: LevDeathRecord,
    ): DeathNotificationDetails {
      with(levDeathRecord) {
        return DeathNotificationDetails(
          enrichmentFields = enrichmentFields,
          registrationDate = if (enrichmentFields.contains(EnrichmentField.REGISTRATION_DATE)) date else null,
          firstNames = if (enrichmentFields.contains(EnrichmentField.FIRST_NAMES)) deceased.forenames else null,
          lastName = if (enrichmentFields.contains(EnrichmentField.LAST_NAME)) deceased.surname else null,
          sex = if (enrichmentFields.contains(EnrichmentField.SEX)) deceased.sex else null,
          dateOfDeath = if (enrichmentFields.contains(EnrichmentField.DATE_OF_DEATH)) deceased.dateOfDeath else null,
          dateOfBirth = if (enrichmentFields.contains(EnrichmentField.DATE_OF_BIRTH)) deceased.dateOfBirth else null,
          birthPlace = if (enrichmentFields.contains(EnrichmentField.BIRTH_PLACE)) deceased.birthplace else null,
          deathPlace = if (enrichmentFields.contains(EnrichmentField.DEATH_PLACE)) deceased.deathplace else null,
          maidenName = if (enrichmentFields.contains(EnrichmentField.MAIDEN_NAME)) deceased.maidenSurname else null,
          occupation = if (enrichmentFields.contains(EnrichmentField.OCCUPATION)) deceased.occupation else null,
          retired = if (enrichmentFields.contains(EnrichmentField.RETIRED)) deceased.retired else null,
          address = if (enrichmentFields.contains(EnrichmentField.ADDRESS)) deceased.address else null,
        )
      }
    }
  }
}

class DeathNotificationDetailsSerializer :
  StdSerializer<DeathNotificationDetails>(DeathNotificationDetails::class.java) {
  override fun serialize(value: DeathNotificationDetails?, gen: JsonGenerator?, provider: SerializerProvider?) {
    gen?.let {
      gen.writeStartObject()
      value?.run {
        if (enrichmentFields.contains(EnrichmentField.REGISTRATION_DATE)) {
          gen.writeNullableStringField(EnrichmentField.REGISTRATION_DATE.jsonName, registrationDate?.toString())
        }
        if (enrichmentFields.contains(EnrichmentField.FIRST_NAMES)) {
          gen.writeNullableStringField(EnrichmentField.FIRST_NAMES.jsonName, firstNames)
        }
        if (enrichmentFields.contains(EnrichmentField.LAST_NAME)) {
          gen.writeNullableStringField(EnrichmentField.LAST_NAME.jsonName, lastName)
        }
        if (enrichmentFields.contains(EnrichmentField.SEX)) {
          gen.writeNullableStringField(EnrichmentField.SEX.jsonName, sex?.jsonName)
        }
        if (enrichmentFields.contains(EnrichmentField.DATE_OF_DEATH)) {
          gen.writeNullableStringField(EnrichmentField.DATE_OF_DEATH.jsonName, dateOfDeath?.toString())
        }
        if (enrichmentFields.contains(EnrichmentField.DATE_OF_BIRTH)) {
          gen.writeNullableStringField(EnrichmentField.DATE_OF_BIRTH.jsonName, dateOfBirth?.toString())
        }
        if (enrichmentFields.contains(EnrichmentField.BIRTH_PLACE)) {
          gen.writeNullableStringField(EnrichmentField.BIRTH_PLACE.jsonName, birthPlace)
        }
        if (enrichmentFields.contains(EnrichmentField.DEATH_PLACE)) {
          gen.writeNullableStringField(EnrichmentField.DEATH_PLACE.jsonName, deathPlace)
        }
        if (enrichmentFields.contains(EnrichmentField.MAIDEN_NAME)) {
          gen.writeNullableStringField(EnrichmentField.MAIDEN_NAME.jsonName, maidenName)
        }
        if (enrichmentFields.contains(EnrichmentField.OCCUPATION)) {
          gen.writeNullableStringField(EnrichmentField.OCCUPATION.jsonName, occupation)
        }
        if (enrichmentFields.contains(EnrichmentField.RETIRED)) {
          gen.writeNullableBooleanField(EnrichmentField.RETIRED.jsonName, retired)
        }
        if (enrichmentFields.contains(EnrichmentField.ADDRESS)) {
          gen.writeNullableStringField(EnrichmentField.ADDRESS.jsonName, address)
        }
      }
      gen.writeEndObject()
    }
  }
}
