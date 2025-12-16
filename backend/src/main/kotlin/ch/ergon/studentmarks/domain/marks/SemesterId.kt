package ch.ergon.studentmarks.domain.marks

import java.util.UUID

data class SemesterId(
    val value: UUID,
) {
    constructor(value: String) : this(UUID.fromString(value))
}
