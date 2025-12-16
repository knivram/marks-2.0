package ch.ergon.studentmarks.db

import ch.ergon.studentmarks.domain.marks.MarkAverage
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.marks.SubjectId
import ch.ergon.studentmarks.domain.subjects.Subject
import ch.ergon.studentmarks.domain.subjects.SubjectOverview
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.gen.Tables.MARK
import ch.ergon.studentmarks.gen.Tables.SUBJECT
import ch.ergon.studentmarks.infrastructure.security.exeptions.SubjectNotFoundException
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.select
import org.jooq.impl.DSL.sum
import org.springframework.stereotype.Service

@Service
class SubjectsRepository(
    private val dsl: DSLContext,
) {
    private fun Record.toSubject(): Subject =
        Subject(
            SubjectId(this[SUBJECT.ID]),
            this[SUBJECT.NAME],
            this[SUBJECT.IS_VOCATIONAL_MODULE],
            this[SUBJECT.DESCRIPTION],
        )

    fun getAllSubjects(): List<Subject> =
        dsl
            .selectFrom(SUBJECT)
            .orderBy(SUBJECT.NAME)
            .fetch()
            .map { it.toSubject() }

    fun getAllSubjectsOfUser(
        userId: UserId,
        semesterId: SemesterId,
    ): List<SubjectOverview> {
        val subquery =
            select(sum(MARK.WEIGHT))
                .from(MARK)
                .where(MARK.SUBJECT_ID.eq(SUBJECT.ID))
                .and(MARK.USER_ID.eq(userId.value))
                .and(MARK.VALUE.isNotNull)
                .and(MARK.SEMESTER_ID.eq(semesterId.value))

        val record =
            dsl
                .select(SUBJECT.asterisk(), (sum(MARK.WEIGHT * MARK.VALUE) / field(subquery)).`as`("avg"))
                .from(MARK)
                .join(SUBJECT)
                .on(MARK.SUBJECT_ID.eq(SUBJECT.ID))
                .where(MARK.USER_ID.eq(userId.value))
                .and(MARK.SEMESTER_ID.eq(semesterId.value))
                .groupBy(SUBJECT.ID, SUBJECT.NAME)
                .orderBy(SUBJECT.NAME)
                .fetch()

        return record.map {
            SubjectOverview(
                SubjectId(it[SUBJECT.ID]),
                it[SUBJECT.NAME],
                it[SUBJECT.IS_VOCATIONAL_MODULE],
                it[SUBJECT.DESCRIPTION],
                MarkAverage(it.getValue("avg", Integer.TYPE)),
            )
        }
    }

    fun getSubject(
        subjectId: SubjectId,
        userId: UserId,
        semesterId: SemesterId,
    ): SubjectOverview {
        val subquery =
            select(sum(MARK.WEIGHT))
                .from(MARK)
                .where(MARK.SUBJECT_ID.eq(SUBJECT.ID))
                .and(MARK.USER_ID.eq(userId.value))
                .and(MARK.VALUE.isNotNull)
                .and(MARK.SEMESTER_ID.eq(semesterId.value))

        val record =
            dsl
                .select(SUBJECT.asterisk(), (sum(MARK.WEIGHT * MARK.VALUE) / field(subquery)).`as`("avg"))
                .from(MARK)
                .join(SUBJECT)
                .on(MARK.SUBJECT_ID.eq(SUBJECT.ID))
                .where(MARK.SUBJECT_ID.eq(subjectId.value))
                .and(MARK.USER_ID.eq(userId.value))
                .and(MARK.SEMESTER_ID.eq(semesterId.value))
                .groupBy(SUBJECT.ID, SUBJECT.NAME)
                .orderBy(SUBJECT.NAME)
                .fetchOne()

        return record?.let {
            SubjectOverview(
                SubjectId(it[SUBJECT.ID]),
                it[SUBJECT.NAME],
                it[SUBJECT.IS_VOCATIONAL_MODULE],
                it[SUBJECT.DESCRIPTION],
                MarkAverage(it.getValue("avg", Integer.TYPE)),
            )
        } ?: throw SubjectNotFoundException(subjectId)
    }

    fun editSubject(subject: Subject): Subject =
        dsl
            .update(SUBJECT)
            .set(SUBJECT.NAME, subject.name)
            .set(SUBJECT.DESCRIPTION, subject.description)
            .set(SUBJECT.IS_VOCATIONAL_MODULE, subject.isVocationalModule)
            .where(SUBJECT.ID.eq(subject.id.value))
            .returning()
            .fetchOne()!!
            .toSubject()

    fun addSubject(subject: Subject): Subject =
        dsl
            .insertInto(SUBJECT)
            .set(SUBJECT.ID, subject.id.value)
            .set(SUBJECT.NAME, subject.name)
            .set(SUBJECT.IS_VOCATIONAL_MODULE, subject.isVocationalModule)
            .set(SUBJECT.DESCRIPTION, subject.description)
            .returning()
            .fetchOne()!!
            .toSubject()
}
