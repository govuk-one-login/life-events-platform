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
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Event Api Audit")
class AcquirerEventAudit(
  @Id
  @Column("id")
  val auditId: UUID = UUID.randomUUID(),

  val oauthClientId: String?,
  val url: String?,
  val requestMethod: String?,
  val queueName: String?,
  val payload: Payload,
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
    val sourceId: String?,
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
