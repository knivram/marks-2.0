package ch.ergon.studentmarks.infrastructure.repository

import ch.ergon.studentmarks.domain.exception.MarkNotFoundException
import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.gen.Tables.MARK
import ch.ergon.studentmarks.infrastructure.repository.mapper.MarkRecordMapper.toMark
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MarkRepositoryImpl(
    private val dsl: DSLContext,
) : MarkRepository {
    override fun getById(markId: MarkId): Mark =
        dsl
            .selectFrom(MARK)
            .where(MARK.ID.eq(markId.value))
            .andNot(MARK.DELETED)
            .fetchOne()
            ?.toMark()
            ?: throw MarkNotFoundException(markId)

    override fun findByUser(
        userId: UserId,
        semesterId: SemesterId?,
        subjectId: SubjectId?,
    ): List<Mark> {
        var selectCondition =
            dsl
                .selectFrom(MARK)
                .where(MARK.USER_ID.eq(userId.value))

        if (semesterId != null) {
            selectCondition = selectCondition.and(MARK.SEMESTER_ID.eq(semesterId.value))
        }
        if (subjectId != null) {
            selectCondition = selectCondition.and(MARK.SUBJECT_ID.eq(subjectId.value))
        }

        return selectCondition
            .andNot(MARK.DELETED)
            .orderBy(MARK.DATE)
            .fetch()
            .map { it.toMark() }
    }

    override fun save(
        userId: UserId,
        mark: Mark,
    ): Mark {
        dsl
            .insertInto(MARK)
            .set(MARK.ID, mark.id.value)
            .set(MARK.USER_ID, userId.value)
            .set(MARK.SUBJECT_ID, mark.subjectId.value)
            .set(MARK.NAME, mark.name)
            .set(MARK.DATE, mark.date)
            .set(MARK.WEIGHT, mark.weight)
            .set(MARK.VALUE, mark.value.value)
            .set(MARK.SEMESTER_ID, mark.semesterId.value)
            .execute()

        return getById(mark.id)
    }

    override fun update(
        userId: UserId,
        mark: Mark,
    ): Mark {
        dsl
            .update(MARK)
            .set(MARK.ID, mark.id.value)
            .set(MARK.USER_ID, userId.value)
            .set(MARK.SUBJECT_ID, mark.subjectId.value)
            .set(MARK.NAME, mark.name)
            .set(MARK.DATE, mark.date)
            .set(MARK.WEIGHT, mark.weight)
            .set(MARK.VALUE, mark.value.value)
            .set(MARK.SEMESTER_ID, mark.semesterId.value)
            .where(MARK.ID.eq(mark.id.value))
            .andNot(MARK.DELETED)
            .execute()

        return getById(mark.id)
    }

    override fun softDelete(markId: MarkId) {
        val updatedRows =
            dsl
                .update(MARK)
                .set(MARK.DELETED, true)
                .set(MARK.DELETED_TIME, LocalDateTime.now())
                .where(MARK.ID.eq(markId.value))
                .andNot(MARK.DELETED)
                .execute()
        if (updatedRows == 0) {
            throw MarkNotFoundException(markId)
        }
    }

    override fun softDeleteByUser(userId: UserId) {
        dsl
            .update(MARK)
            .set(MARK.DELETED, true)
            .set(MARK.DELETED_TIME, LocalDateTime.now())
            .where(MARK.USER_ID.eq(userId.value))
            .execute()
    }

    override fun restore(markId: MarkId): Mark {
        val updatedRows =
            dsl
                .update(MARK)
                .set(MARK.DELETED, false)
                .setNull(MARK.DELETED_TIME)
                .where(MARK.ID.eq(markId.value))
                .and(MARK.DELETED)
                .execute()
        if (updatedRows == 0) {
            throw MarkNotFoundException(markId)
        }

        return getById(markId)
    }

    override fun findDeleted(userId: UserId): List<Mark> =
        dsl
            .selectFrom(MARK)
            .where(MARK.USER_ID.eq(userId.value))
            .and(MARK.DELETED)
            .orderBy(MARK.DATE)
            .fetch()
            .map { it.toMark() }

    override fun getOwnerUserId(markId: MarkId): UserId =
        dsl
            .select(MARK.USER_ID)
            .from(MARK)
            .where(MARK.ID.eq(markId.value))
            .fetchOne(MARK.USER_ID)
            ?.let { UserId(it) }
            ?: throw MarkNotFoundException(markId)

    override fun deleteExpiredSoftDeleted(olderThan: LocalDateTime) {
        dsl
            .deleteFrom(MARK)
            .where(MARK.DELETED_TIME.lessThan(olderThan))
            .execute()
    }
}
