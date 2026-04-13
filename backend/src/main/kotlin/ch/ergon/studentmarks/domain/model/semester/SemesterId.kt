package ch.ergon.studentmarks.domain.model.semester

import java.util.UUID

data class SemesterId(
    val value: UUID,
) {
    constructor(value: String) : this(UUID.fromString(value))
}
