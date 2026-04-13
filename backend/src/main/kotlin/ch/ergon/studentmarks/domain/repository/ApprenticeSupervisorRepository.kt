package ch.ergon.studentmarks.domain.repository

import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPairId
import ch.ergon.studentmarks.domain.model.user.UserId

interface ApprenticeSupervisorRepository {
    fun connect(
        apprenticeId: UserId,
        supervisorId: UserId,
    ): ApprenticeSupervisorPair

    fun findBySupervisor(
        supervisorId: UserId,
        includeUnconnectedForAdmin: Boolean,
    ): List<ApprenticeSupervisorPair>

    fun findAll(): List<ApprenticeSupervisorPair>

    fun updateOrInsert(pair: ApprenticeSupervisorPair)

    fun delete(pairId: ApprenticeSupervisorPairId): ApprenticeSupervisorPair
}
