package ch.ergon.studentmarks.application.query

import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.UserId
import java.time.LocalDate

interface MarkQueryRepository {
    fun getById(markId: MarkId): MarkView

    fun findByUser(
        userId: UserId,
        semesterId: SemesterId,
        subjectId: SubjectId? = null,
    ): List<MarkView>

    fun findUpcomingExams(
        userId: UserId,
        asOf: LocalDate,
    ): List<MarkView>

    fun findPastExamsWithoutGrade(
        userId: UserId,
        asOf: LocalDate,
    ): List<MarkView>
}
