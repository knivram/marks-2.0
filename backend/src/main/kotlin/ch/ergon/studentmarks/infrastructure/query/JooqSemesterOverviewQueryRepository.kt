package ch.ergon.studentmarks.infrastructure.query

import ch.ergon.studentmarks.application.query.model.MarkWithSubject
import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.Subject
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.gen.Tables.MARK
import ch.ergon.studentmarks.gen.Tables.SUBJECT
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository

@Repository
class JooqSemesterOverviewQueryRepository(
    private val dsl: DSLContext,
) {
    fun getMarksWithSubjects(
        userId: UserId,
        semesterId: SemesterId,
        subjectId: SubjectId? = null,
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
