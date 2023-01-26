package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.format.annotation.DateTimeFormat
import uk.gov.gdx.datashare.enums.Sex
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Death notification")
data class DeathNotificationDetails(

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
)
