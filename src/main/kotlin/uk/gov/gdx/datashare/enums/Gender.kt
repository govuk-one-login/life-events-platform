package uk.gov.gdx.datashare.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class Gender(@JsonValue val jsonName: String) {
  MALE("Male"),
  FEMALE("Female"),
  INDETERMINATE("Indeterminate"),
  ;

  companion object {
    fun parse(value: String) = when (value) {
      "Female" -> FEMALE
      "Male" -> MALE
      else -> INDETERMINATE
    }
  }
}
