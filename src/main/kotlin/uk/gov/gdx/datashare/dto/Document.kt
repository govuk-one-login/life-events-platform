package uk.gov.gdx.datashare.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.hateoas.Link

@JsonInclude(JsonInclude.Include.NON_NULL)
class Document<T>(
  val data: T? = null,
  val errors: List<String>? = null,
  val links: Map<String, Link>? = null,
) {
  init {
    if (data != null && errors != null) {
      throw IllegalArgumentException("Only one of data and errors may be specified")
    }
  }
}
