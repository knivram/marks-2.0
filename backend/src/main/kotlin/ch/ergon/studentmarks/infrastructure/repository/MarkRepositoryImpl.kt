package ch.ergon.studentmarks.infrastructure.repository

import ch.ergon.studentmarks.application.response.MarkWithSubject
import ch.ergon.studentmarks.domain.exception.MarkNotFoundException
import ch.ergon.studentmarks.domain.exception.UserNotFoundException
import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.Subject
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.gen.Tables.MARK
import ch.ergon.studentmarks.gen.Tables.SUBJECT
import ch.ergon.studentmarks.infrastructure.repository.mapper.MarkRecordMapper.toMark
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.inline
import org.springframework.stereotype.Repository
import java.time.LocalDate
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
            .execute()

        return getById(mark.id)
    }

    override fun softDelete(markId: MarkId) {
        dsl
            .update(MARK)
            .set(MARK.DELETED, true)
            .set(MARK.DELETED_TIME, LocalDateTime.now())
            .where(MARK.ID.eq(markId.value))
            .execute()
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
        dsl
            .update(MARK)
            .set(MARK.DELETED, false)
            .setNull(MARK.DELETED_TIME)
            .where(inline(true))
            .and(MARK.ID.eq(markId.value))
            .returning(MARK.asterisk())
            .fetchOne()!!

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
            .selectFrom(MARK)
            .where(MARK.ID.eq(markId.value))
            .fetchOne()
            ?.let { UserId(it.userId) }
            ?: throw UserNotFoundException(
                searchParam = markId.value.toString(),
                message = "The owner of mark with id \"${markId.value}\" was not found.",
            )

    override fun deleteExpiredSoftDeleted(olderThan: LocalDateTime) {
        dsl
            .deleteFrom(MARK)
            .where(MARK.DELETED_TIME.lessThan(olderThan))
            .execute()
    }

    override fun findWithSubjects(
        userId: UserId,
        semesterId: SemesterId,
        subjectId: SubjectId?,
    ): List<MarkWithSubject> {
        var query =
            dsl
                .select(MARK.asterisk(), SUBJECT.asterisk())
                .from(MARK)
                .join(SUBJECT)
                .on(MARK.SUBJECT_ID.eq(SUBJECT.ID))
                .where(MARK.USER_ID.eq(userId.value))
                .and(MARK.SEMESTER_ID.eq(semesterId.value))
                .andNot(MARK.DELETED)

        if (subjectId != null) {
            query = query.and(MARK.SUBJECT_ID.eq(subjectId.value))
        }

        return query
            .orderBy(SUBJECT.NAME, MARK.DATE)
            .fetch()
            .map { it.toMarkWithSubject() }
    }

    override fun findUpcomingExams(
        userId: UserId,
        asOf: LocalDate,
    ): List<MarkWithSubject> =
        dsl
            .select(MARK.asterisk(), SUBJECT.asterisk())
            .from(MARK)
            .join(SUBJECT)
            .on(MARK.SUBJECT_ID.eq(SUBJECT.ID))
            .where(MARK.USER_ID.eq(userId.value))
            .and(MARK.DATE.greaterOrEqual(asOf))
            .andNot(MARK.DELETED)
            .orderBy(MARK.DATE)
            .fetch()
            .map { it.toMarkWithSubject() }

    override fun findPastExamsWithoutGrade(
        userId: UserId,
        asOf: LocalDate,
    ): List<MarkWithSubject> =
        dsl
            .select(MARK.asterisk(), SUBJECT.asterisk())
            .from(MARK)
            .join(SUBJECT)
            .on(MARK.SUBJECT_ID.eq(SUBJECT.ID))
            .where(MARK.USER_ID.eq(userId.value))
            .and(MARK.VALUE.isNull)
            .and(MARK.DATE.lessThan(asOf))
            .andNot(MARK.DELETED)
            .orderBy(MARK.DATE)
            .fetch()
            .map { it.toMarkWithSubject() }

    private fun Record.toMarkWithSubject(): MarkWithSubject =
        MarkWithSubject(
            mark =
                Mark(
                    id = MarkId(this[MARK.ID]),
                    name = this[MARK.NAME],
                    subjectId = SubjectId(this[MARK.SUBJECT_ID]),
                    date = this[MARK.DATE],
                    weight = this[MARK.WEIGHT],
                    value = MarkValue(this[MARK.VALUE]),
                    semesterId = SemesterId(this[MARK.SEMESTER_ID]),
                ),
            subject =
                Subject(
                    id = SubjectId(this[SUBJECT.ID]),
                    name = this[SUBJECT.NAME],
                    isVocationalModule = this[SUBJECT.IS_VOCATIONAL_MODULE],
                    description = this[SUBJECT.DESCRIPTION],
                ),
        )
}
