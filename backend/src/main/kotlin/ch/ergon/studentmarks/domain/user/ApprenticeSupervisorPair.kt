package ch.ergon.studentmarks.domain.user

import java.util.UUID

data class ApprenticeSupervisorPair(
    val id: ApprenticeSupervisorPairId,
    val apprentice: User,
    val supervisor: User,
    val emailNotification: Boolean,
) {
    constructor(id: String, apprentice: User, supervisor: User, emailNotification: Boolean) :
        this(ApprenticeSupervisorPairId(id), apprentice, supervisor, emailNotification)

    constructor(id: UUID, apprentice: User, supervisor: User, emailNotification: Boolean) :
        this(ApprenticeSupervisorPairId(id), apprentice, supervisor, emailNotification)
}
