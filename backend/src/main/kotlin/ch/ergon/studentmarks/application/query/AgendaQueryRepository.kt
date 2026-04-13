package ch.ergon.studentmarks.application.query

import ch.ergon.studentmarks.application.query.model.MarkWithSubject
import ch.ergon.studentmarks.domain.model.user.UserId
import java.time.LocalDate

interface AgendaQueryRepository {
    fun getUpcomingExams(
        userId: UserId,
        asOf: LocalDate,
    ): List<MarkWithSubject>

    fun getPastExamsWithoutGrade(
        userId: UserId,
        asOf: LocalDate,
    ): List<MarkWithSubject>
}
