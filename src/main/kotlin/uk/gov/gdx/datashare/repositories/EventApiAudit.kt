package uk.gov.gdx.datashare.repositories

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Schema
import org.postgresql.util.PGobject
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.convert.converter.Converter
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Event Api Audit")
class EventApiAudit(
  @Id
  @Column("id")
  @Schema(description = "Audit ID", required = true, example = "00000000-0000-0001-0000-000000000000", pattern = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$")
  val auditId: UUID = UUID.randomUUID(),

  @Schema(description = "Oauth ID of client making call", required = true, example = "alskd987", maxLength = 50, pattern = "^[a-zA-Z0-9-_]{50}\$")
  val oauthClientId: String,
  @Schema(description = "URL", required = true, example = "https://d33v84mi0vopmk.cloudfront.net/events")
  val url: String,
  @Schema(description = "METHOD", required = true, example = "GET")
  val requestMethod: String,
  @Schema(description = "Data of event(s)", required = true, example = "{\"data\":[]}")
  val payload: Payload,

  @Schema(description = "When audit log created", required = true)
  val whenCreated: LocalDateTime,

  @Transient
  @Value("false")
  @JsonIgnore
  val new: Boolean = true,

) : Persistable<UUID> {

  @JsonIgnore
  override fun getId(): UUID = auditId

  override fun isNew(): Boolean = new

  data class Payload(
    val data: List<Data>,
  )
  data class Data(
    val eventId: UUID,
    val sourceId: String,
    val hashedEventData: String?,
  )

  @WritingConverter
  class EntityWritingConverter(
    private val objectMapper: ObjectMapper,
  ) : Converter<Payload, PGobject> {
    override fun convert(source: Payload): PGobject? {
      val jsonObject = PGobject()
      jsonObject.type = "json"
      jsonObject.value = objectMapper.writeValueAsString(source)
      return jsonObject
    }
  }

  @ReadingConverter
  class EntityReadingConverter(
    private val objectMapper: ObjectMapper,
  ) : Converter<PGobject, Payload> {
    override fun convert(pgObject: PGobject): Payload {
      val source = pgObject.value
      return objectMapper.readValue(source, Payload::class.java)
    }
  }
}
