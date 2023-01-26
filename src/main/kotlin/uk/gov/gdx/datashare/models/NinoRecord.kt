package uk.gov.gdx.datashare.models

import java.util.UUID

data class NinoRecord(
    val id: UUID,
    val ni_number: String,
)
