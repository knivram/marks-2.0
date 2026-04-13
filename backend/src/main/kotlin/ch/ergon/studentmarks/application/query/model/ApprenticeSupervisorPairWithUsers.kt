package ch.ergon.studentmarks.application.query.model

import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.model.user.User

data class ApprenticeSupervisorPairWithUsers(
    val pair: ApprenticeSupervisorPair,
    val apprentice: User,
    val supervisor: User,
)
