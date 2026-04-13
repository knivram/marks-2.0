package ch.ergon.studentmarks.domain.model.user

import java.util.UUID

data class ApprenticeSupervisorPairId(
    val value: UUID,
) {
    constructor(value: String) :
        this(UUID.fromString(value))
}
