package ch.ergon.studentmarks.domain.subjects

import ch.ergon.studentmarks.domain.marks.MarkAverage
import ch.ergon.studentmarks.domain.marks.SubjectId

data class SubjectOverview(
    val subjectId: SubjectId,
    val name: String,
    val isVocationalModule: Boolean,
    val description: String?,
    val markAverage: MarkAverage,
)
