package ch.ergon.studentmarks.api.subjects

import ch.ergon.studentmarks.domain.marks.Mark
import java.util.UUID

data class FullSubjectDto(
    val subjectId: UUID,
    val subjectName: String,
    val isVocationalModule: Boolean,
    val description: String?,
    val markAverage: Int,
    val marks: List<Mark>,
)
