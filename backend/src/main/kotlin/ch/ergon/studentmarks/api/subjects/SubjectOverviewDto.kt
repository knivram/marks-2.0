package ch.ergon.studentmarks.api.subjects

import java.util.UUID

data class SubjectOverviewDto(
    val subjectId: UUID,
    val name: String,
    val isVocationalModule: Boolean,
    val description: String?,
    val markAverage: Int,
)
