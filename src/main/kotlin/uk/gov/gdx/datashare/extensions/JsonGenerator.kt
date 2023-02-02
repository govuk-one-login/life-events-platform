package uk.gov.gdx.datashare.extensions

import com.fasterxml.jackson.core.JsonGenerator

fun JsonGenerator.writeNullableStringField(fieldName: String, value: String?) {
  if (value == null) {
    writeNullField(fieldName)
  } else {
    writeStringField(fieldName, value)
  }
}

fun JsonGenerator.writeNullableBooleanField(fieldName: String, value: Boolean?) {
  if (value == null) {
    writeNullField(fieldName)
  } else {
    writeBooleanField(fieldName, value)
  }
}
