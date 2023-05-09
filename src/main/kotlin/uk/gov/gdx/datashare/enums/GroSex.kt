package uk.gov.gdx.datashare.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class GroSex(@JsonValue val jsonName: String) {
  MALE("M"),
  FEMALE("F"),
}
