package uk.gov.gdx.datashare.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class Gender(@JsonValue val jsonName: String) {
  MALE("Male"),
  FEMALE("Female"),
  INDETERMINATE("Indeterminate"),
}
