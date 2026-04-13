package ch.ergon.studentmarks.application.query

import ch.ergon.studentmarks.application.query.model.DashboardAverages
import ch.ergon.studentmarks.application.query.model.MarkWithSubject
import ch.ergon.studentmarks.application.query.model.SemesterOverview
import ch.ergon.studentmarks.application.query.model.SubjectAverage
import ch.ergon.studentmarks.application.query.model.SubjectWithMarks
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.infrastructure.query.JooqSemesterOverviewQueryRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class SemesterOverviewQueryService(
    private val queryRepository: JooqSemesterOverviewQueryRepository,
) {
    @PreAuthorize(
        "hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')",
    )
    fun getSemesterOverview(
        userId: UserId,
        semesterId: SemesterId,
    ): SemesterOverview {
        val marksWithSubjects = queryRepository.getMarksWithSubjects(userId, semesterId)
        return buildSemesterOverview(marksWithSubjects)
    }

    @PreAuthorize(
        "hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')",
    )
    fun getDashboardAverages(
        userId: UserId,
        semesterId: SemesterId,
    ): DashboardAverages {
        val marksWithSubjects = queryRepository.getMarksWithSubjects(userId, semesterId)
        return buildDashboardAverages(marksWithSubjects)
    }

    private fun buildSemesterOverview(marksWithSubjects: List<MarkWithSubject>): SemesterOverview {
        val bySubject = marksWithSubjects.groupBy { it.subject }

        val subjectViews =
            bySubject.map { (subject, items) ->
                val marks = items.map { it.mark }
                SubjectWithMarks(
                    subject = subject,
                    average = AverageCalculator.weightedAverage(marks),
                    marks = marks,
                )
            }

        val (vocational, general) = subjectViews.partition { it.subject.isVocationalModule }

        return SemesterOverview(
            vocationalSubjects = vocational,
            generalSubjects = general,
            vocationalAverage = AverageCalculator.groupAverage(vocational.map { it.average }),
            generalAverage = AverageCalculator.groupAverage(general.map { it.average }),
        )
    }

    private fun buildDashboardAverages(marksWithSubjects: List<MarkWithSubject>): DashboardAverages {
        val bySubject = marksWithSubjects.groupBy { it.subject }

        val subjectAverages =
            bySubject.map { (subject, items) ->
                SubjectAverage(
                    subject = subject,
                    average = AverageCalculator.weightedAverage(items.map { it.mark }),
                )
            }

        val (vocational, general) = subjectAverages.partition { it.subject.isVocationalModule }

        return DashboardAverages(
            vocationalSubjects = vocational,
            generalSubjects = general,
            vocationalAverage = AverageCalculator.groupAverage(vocational.map { it.average }),
            generalAverage = AverageCalculator.groupAverage(general.map { it.average }),
        )
    }
}
