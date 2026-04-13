package ch.ergon.studentmarks.application.query

import ch.ergon.studentmarks.application.query.model.Agenda
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.infrastructure.query.JooqAgendaQueryRepository
import ch.ergon.studentmarks.utils.TimeUtils
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class AgendaQueryService(
    private val queryRepository: JooqAgendaQueryRepository,
    private val timeUtils: TimeUtils,
) {
    @PreAuthorize(
        "hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')",
    )
    fun getAgenda(userId: UserId): Agenda =
        Agenda(
            pastExamsWithoutGrade = queryRepository.getPastExamsWithoutGrade(userId, timeUtils.now()),
            upcomingExams = queryRepository.getUpcomingExams(userId, timeUtils.now()),
        )
}
