package ch.ergon.studentmarks.application.query

import ch.ergon.studentmarks.application.query.model.MarkWithSubject
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.UserId

interface SemesterOverviewQueryRepository {
    fun getMarksWithSubjects(
        userId: UserId,
        semesterId: SemesterId,
        subjectId: SubjectId? = null,
    ): List<MarkWithSubject>
}
