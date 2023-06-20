package uk.gov.gdx.datashare.config

import java.security.MessageDigest

fun String.sha256(): String {
  return MessageDigest
    .getInstance("SHA-256")
    .digest(this.toByteArray())
    .fold(StringBuilder()) { sb, it -> sb.append("%02x".format(it)) }
    .toString()
}
