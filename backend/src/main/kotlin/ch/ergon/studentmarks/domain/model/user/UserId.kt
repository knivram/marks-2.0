package ch.ergon.studentmarks.domain.model.user

import java.util.UUID

data class UserId(
    val value: UUID,
) {
    constructor(id: String) : this(UUID.fromString(id))
}
