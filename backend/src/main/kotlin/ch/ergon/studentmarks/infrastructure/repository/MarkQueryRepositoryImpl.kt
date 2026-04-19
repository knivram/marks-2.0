package ch.ergon.studentmarks.infrastructure.repository

import ch.ergon.studentmarks.application.query.MarkQueryRepository
import ch.ergon.studentmarks.application.query.MarkView
import ch.ergon.studentmarks.domain.exception.MarkNotFoundException
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.gen.Tables.MARK
import ch.ergon.studentmarks.gen.Tables.SUBJECT
import ch.ergon.studentmarks.infrastructure.repository.mapper.MarkRecordMapper.toMark
import ch.ergon.studentmarks.infrastructure.repository.mapper.SubjectRecordMapper.toSubject
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class MarkQueryRepositoryImpl(
    private val dsl: DSLContext,
) : MarkQueryRepository {
    override fun getById(markId: MarkId): MarkView =
        dsl
            .select(MARK.asterisk(), SUBJECT.asterisk())
            .from(MARK)
            .join(SUBJECT)
            .on(MARK.SUBJECT_ID.eq(SUBJECT.ID))
            .where(MARK.ID.eq(markId.value))
            .andNot(MARK.DELETED)
            .fetchOne()
            ?.toMarkView()
            ?: throw MarkNotFoundException(markId)

    override fun findByUser(
        userId: UserId,
        semesterId: SemesterId,
        subjectId: SubjectId?,
    ): List<MarkView> {
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
            .map { it.toMarkView() }
    }

    override fun findUpcomingExams(
        userId: UserId,
        asOf: LocalDate,
    ): List<MarkView> =
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
            .map { it.toMarkView() }

    override fun findPastExamsWithoutGrade(
        userId: UserId,
        asOf: LocalDate,
    ): List<MarkView> =
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
            .map { it.toMarkView() }

    private fun Record.toMarkView(): MarkView {
        val mark = toMark()
        return MarkView(
            id = mark.id,
            name = mark.name,
            subject = toSubject(),
            date = mark.date,
            weight = mark.weight,
            value = mark.value,
            semesterId = mark.semesterId,
        )
    }
}
