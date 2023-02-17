package uk.gov.gdx.datashare.enums

object RegExConstants {
  const val UUID_REGEX = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}\$"
  const val EVENT_TYPE_REGEX = "[A-Z_]{1,40}"
  const val SUP_ACQ_NAME_REGEX = "^[a-zA-Z\\d. _-]{1,80}\$"
  const val CLIENT_ID_REGEX = "^[a-zA-Z0-9-_]{1,50}\$"
  const val DIGITS_REGEX = "^\\d+\$"
  const val CLIENT_NAME_REGEX = "^[a-z0-9+=,.@-]*\$"
  const val CLIENT_SECRET_REGEX = "^[a-zA-Z0-9]{1,80}\$"
  const val SOURCE_ID_REGEX = "^[A-Za-z0-9+=,.@-_]*\$"
}
