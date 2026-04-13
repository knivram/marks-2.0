package ch.ergon.studentmarks.domain.model.subject

import java.util.UUID

data class SubjectId(
    val value: UUID,
) {
    constructor(value: String) : this(UUID.fromString(value))
}
