package ch.ergon.studentmarks.domain.repository

import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.UserId
import java.time.LocalDateTime

interface MarkRepository {
    fun findById(markId: MarkId): Mark

    fun findByUser(
        userId: UserId,
        semesterId: SemesterId? = null,
        subjectId: SubjectId? = null,
    ): List<Mark>

    fun save(
        userId: UserId,
        mark: Mark,
    ): Mark

    fun update(
        userId: UserId,
        mark: Mark,
    ): Mark

    fun softDelete(markId: MarkId)

    fun softDeleteByUser(userId: UserId)

    fun restore(markId: MarkId): Mark

    fun findDeleted(userId: UserId): List<Mark>

    fun getOwnerUserId(markId: MarkId): UserId

    fun deleteExpiredSoftDeleted(olderThan: LocalDateTime)
}
