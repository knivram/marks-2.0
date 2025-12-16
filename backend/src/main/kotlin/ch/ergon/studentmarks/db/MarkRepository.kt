package ch.ergon.studentmarks.db

import ch.ergon.studentmarks.domain.marks.Mark
import ch.ergon.studentmarks.domain.marks.MarkId
import ch.ergon.studentmarks.domain.marks.MarkValue
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.marks.SubjectId
import ch.ergon.studentmarks.domain.subjects.Subject
import ch.ergon.studentmarks.domain.user.User
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.gen.Tables.MARK
import ch.ergon.studentmarks.gen.Tables.SUBJECT
import ch.ergon.studentmarks.infrastructure.security.exeptions.MarkNotFoundException
import ch.ergon.studentmarks.infrastructure.security.exeptions.UserNotFoundException
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.inline
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class MarkRepository(
    private val dsl: DSLContext,
) {
    private fun Record.toMark(): Mark =
        Mark(
            id = MarkId(this[MARK.ID]),
            name = this[MARK.NAME],
            subject =
                Subject(
                    SubjectId(this[MARK.SUBJECT_ID]),
                    this[SUBJECT.NAME],
                    this[SUBJECT.IS_VOCATIONAL_MODULE],
                    this[SUBJECT.DESCRIPTION],
                ),
            date = this[MARK.DATE],
            weight = this[MARK.WEIGHT],
            value = MarkValue(this[MARK.VALUE]),
            semesterId = SemesterId(this[MARK.SEMESTER_ID]),
        )

    fun getOwnerOfMark(markId: MarkId): UserId =
        dsl
            .selectFrom(MARK)
            .where(MARK.ID.eq(markId.value))
            .fetchOne()
            ?.let { UserId(it.userId) }
            ?: throw UserNotFoundException(
                searchParam = markId.value.toString(),
                message = "The owner off mark with id \"${markId.value}\" was not found.",
            )

    fun getMark(markId: MarkId): Mark {
        val recordMark =
            dsl
                .select(MARK.asterisk(), SUBJECT.asterisk())
                .from(MARK)
                .join(SUBJECT)
                .on(MARK.SUBJECT_ID.eq(SUBJECT.ID))
                .where(MARK.ID.eq(markId.value))
                .andNot(MARK.DELETED)
                .fetchOne()

        return recordMark?.toMark() ?: throw MarkNotFoundException(markId)
    }

    fun getMarks(
        userId: UserId,
        semesterId: SemesterId? = null,
        subjectId: SubjectId? = null,
    ): List<Mark> {
        var selectCondition =
            dsl
                .select(MARK.asterisk(), SUBJECT.asterisk())
                .from(MARK)
                .join(SUBJECT)
                .on(SUBJECT.ID.eq(MARK.SUBJECT_ID))
                .where(MARK.USER_ID.eq(userId.value))

        if (semesterId != null) {
            selectCondition = selectCondition.and(MARK.SEMESTER_ID.eq(semesterId.value))
        }

        if (subjectId != null) {
            selectCondition = selectCondition.and(MARK.SUBJECT_ID.eq(subjectId.value))
        }

        val record = selectCondition.andNot(MARK.DELETED).orderBy(MARK.DATE).fetch()

        return record.map { it.toMark() }
    }

    fun getFutureMarks(
        userId: UserId,
        dateToday: LocalDate,
    ): List<Mark> {
        val record =
            dsl
                .select(MARK.asterisk(), SUBJECT.asterisk())
                .from(MARK)
                .join(SUBJECT)
                .on(MARK.SUBJECT_ID.eq(SUBJECT.ID))
                .where(MARK.USER_ID.eq(userId.value))
                .and(MARK.DATE.greaterOrEqual(dateToday))
                .andNot(MARK.DELETED)
                .fetch()

        return record.map { it.toMark() }
    }

    fun getPassedExamsWithoutValue(
        userId: UserId,
        dateToday: LocalDate,
    ): List<Mark> {
        val record =
            dsl
                .select(MARK.asterisk(), SUBJECT.asterisk())
                .from(MARK)
                .join(SUBJECT)
                .on(MARK.SUBJECT_ID.eq(SUBJECT.ID))
                .where(MARK.USER_ID.eq(userId.value))
                .and(MARK.VALUE.isNull)
                .and(MARK.DATE.lessThan(dateToday))
                .andNot(MARK.DELETED)
                .fetch()

        return record.map { it.toMark() }
    }

    fun getPassedExamsWithValue(
        userId: UserId,
        dateToday: LocalDate,
        limit: Number,
    ): List<Mark> {
        val record =
            dsl
                .select(MARK.asterisk(), SUBJECT.asterisk())
                .from(MARK)
                .join(SUBJECT)
                .on(MARK.SUBJECT_ID.eq(SUBJECT.ID))
                .where(MARK.USER_ID.eq(userId.value))
                .and(MARK.VALUE.isNotNull)
                .and(MARK.DATE.lessOrEqual(dateToday))
                .andNot(MARK.DELETED)
                .orderBy(MARK.DATE.desc())
                .limit(limit)
                .fetch()

        return record.map { it.toMark() }
    }

    fun newMarkForUser(
        user: User,
        mark: Mark,
    ): Mark {
        dsl
            .insertInto(MARK)
            .set(MARK.ID, mark.id.value)
            .set(MARK.USER_ID, user.userId.value)
            .set(MARK.SUBJECT_ID, mark.subject.id.value)
            .set(MARK.NAME, mark.name)
            .set(MARK.DATE, mark.date)
            .set(MARK.WEIGHT, mark.weight)
            .set(MARK.VALUE, mark.value.value)
            .set(MARK.SEMESTER_ID, mark.semesterId.value)
            .execute()

        return getMark(mark.id)
    }

    fun editMarkForUser(
        user: User,
        mark: Mark,
    ): Mark {
        dsl
            .update(MARK)
            .set(MARK.ID, mark.id.value)
            .set(MARK.USER_ID, user.userId.value)
            .set(MARK.SUBJECT_ID, mark.subject.id.value)
            .set(MARK.NAME, mark.name)
            .set(MARK.DATE, mark.date)
            .set(MARK.WEIGHT, mark.weight)
            .set(MARK.VALUE, mark.value.value)
            .where(MARK.ID.eq(mark.id.value))
            .execute()

        return getMark(mark.id)
    }

    private fun deleteMark(
        markId: MarkId? = null,
        userId: UserId? = null,
    ) {
        var markUpdateCondition =
            dsl
                .update(MARK)
                .set(MARK.DELETED, true)
                .set(MARK.DELETED_TIME, LocalDateTime.now())
                .where(inline(true))
        if (markId != null) markUpdateCondition = markUpdateCondition.and(MARK.ID.eq(markId.value))
        if (userId != null) markUpdateCondition = markUpdateCondition.and(MARK.USER_ID.eq(userId.value))
        markUpdateCondition.execute()
    }

    fun deleteMarksBy(markId: MarkId) {
        deleteMark(markId = markId)
    }

    fun deleteMarksBy(userId: UserId) {
        deleteMark(userId = userId)
    }

    fun getDeletedMarks(userId: UserId): List<Mark> {
        val marksRecord =
            dsl
                .select(MARK.asterisk(), SUBJECT.asterisk())
                .from(MARK)
                .join(SUBJECT)
                .on(SUBJECT.ID.eq(MARK.SUBJECT_ID))
                .where(MARK.USER_ID.eq(userId.value))
                .and(MARK.DELETED)
                .orderBy(MARK.DATE)
                .fetch()

        return marksRecord.map { it.toMark() }
    }

    fun restoreMarksBy(markId: MarkId): Mark {
        val markUpdateCondition =
            dsl
                .update(MARK)
                .set(MARK.DELETED, false)
                .setNull(MARK.DELETED_TIME)
                .where(inline(true))
                .and(MARK.ID.eq(markId.value))
        val editedMarkId = MarkId(markUpdateCondition.returning(MARK.asterisk()).fetchOne()!!.id)
        return getMark(editedMarkId)
    }

    fun deleteExpiredRemovedMark(expiredTimeEnd: LocalDateTime) {
        dsl
            .deleteFrom(MARK)
            .where(MARK.DELETED_TIME.lessThan(expiredTimeEnd))
            .execute()
    }
}
