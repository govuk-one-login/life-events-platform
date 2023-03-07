package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.format.annotation.DateTimeFormat
import uk.gov.gdx.datashare.enums.EnrichmentField
import uk.gov.gdx.datashare.enums.Gender
import uk.gov.gdx.datashare.extensions.writeNullableStringField
import java.time.LocalDate

@Schema(description = "Prisoner Details")
@JsonSerialize(using = PrisonerDetailsSerializer::class)
data class PrisonerDetails(
  val enrichmentFields: List<EnrichmentField>,

  @Schema(description = "Prisoner Number", required = true, example = "A1234BB")
  val prisonerNumber: String? = null,

  @Schema(description = "Forenames of the prisoner", required = true, example = "Bob")
  val firstName: String? = null,

  @Schema(description = "Middle names of the prisoner", required = true, example = "Bert Paul")
  val middleNames: String? = null,

  @Schema(description = "Surname of the prisoner", required = true, example = "Smith")
  val lastName: String? = null,

  @Schema(description = "Gender of the prisoner", required = true, example = "Female")
  val gender: Gender? = null,

  @Schema(description = "Date the prisoner was born", required = false, example = "2001-12-31", type = "date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  val dateOfBirth: LocalDate? = null,
) : EventDetails {
  companion object : EventDetailsCompanionObject<PrisonerDetails, PrisonerRecord> {
    override fun from(enrichmentFields: List<EnrichmentField>, data: PrisonerRecord): PrisonerDetails {
      with(data) {
        return PrisonerDetails(
          enrichmentFields = enrichmentFields,
          prisonerNumber = if (enrichmentFields.contains(EnrichmentField.PRISONER_NUMBER)) this.prisonerNumber else null,
          firstName = if (enrichmentFields.contains(EnrichmentField.FIRST_NAME)) this.firstName else null,
          middleNames = if (enrichmentFields.contains(EnrichmentField.MIDDLE_NAMES)) this.middleNames else null,
          lastName = if (enrichmentFields.contains(EnrichmentField.LAST_NAME)) this.lastName else null,
          gender = if (enrichmentFields.contains(EnrichmentField.GENDER)) Gender.parse(this.gender) else null,
          dateOfBirth = if (enrichmentFields.contains(EnrichmentField.DATE_OF_BIRTH)) this.dateOfBirth else null,
        )
      }
    }
  }
}

class PrisonerDetailsSerializer :
  StdSerializer<PrisonerDetails>(PrisonerDetails::class.java) {
  override fun serialize(value: PrisonerDetails?, gen: JsonGenerator?, provider: SerializerProvider?) {
    gen?.let {
      gen.writeStartObject()
      value?.run {
        if (enrichmentFields.contains(EnrichmentField.PRISONER_NUMBER)) {
          gen.writeNullableStringField(EnrichmentField.PRISONER_NUMBER.jsonName, prisonerNumber)
        }
        if (enrichmentFields.contains(EnrichmentField.FIRST_NAME)) {
          gen.writeNullableStringField(EnrichmentField.FIRST_NAME.jsonName, firstName)
        }
        if (enrichmentFields.contains(EnrichmentField.MIDDLE_NAMES)) {
          gen.writeNullableStringField(EnrichmentField.MIDDLE_NAMES.jsonName, middleNames)
        }
        if (enrichmentFields.contains(EnrichmentField.LAST_NAME)) {
          gen.writeNullableStringField(EnrichmentField.LAST_NAME.jsonName, lastName)
        }
        if (enrichmentFields.contains(EnrichmentField.GENDER)) {
          gen.writeNullableStringField(EnrichmentField.GENDER.jsonName, gender?.jsonName)
        }
        if (enrichmentFields.contains(EnrichmentField.DATE_OF_BIRTH)) {
          gen.writeNullableStringField(EnrichmentField.DATE_OF_BIRTH.jsonName, dateOfBirth?.toString())
        }
      }
      gen.writeEndObject()
    }
  }
}
