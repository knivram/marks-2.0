package ch.ergon.studentmarks.domain.user

import java.util.UUID

data class UserId(
    val value: UUID,
) {
    constructor(id: String) : this(UUID.fromString(id))
}
