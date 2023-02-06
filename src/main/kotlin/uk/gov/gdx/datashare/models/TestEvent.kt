package uk.gov.gdx.datashare.models

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TestEvent(
  val testField: String? = null,
)
