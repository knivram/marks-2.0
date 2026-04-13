package ch.ergon.studentmarks.domain.model.user

import java.util.UUID

data class ApprenticeSupervisorPair(
    val id: ApprenticeSupervisorPairId,
    val apprenticeId: UserId,
    val supervisorId: UserId,
    val emailNotification: Boolean,
) {
    constructor(id: String, apprenticeId: UserId, supervisorId: UserId, emailNotification: Boolean) :
        this(ApprenticeSupervisorPairId(id), apprenticeId, supervisorId, emailNotification)

    constructor(id: UUID, apprenticeId: UserId, supervisorId: UserId, emailNotification: Boolean) :
        this(ApprenticeSupervisorPairId(id), apprenticeId, supervisorId, emailNotification)
}
