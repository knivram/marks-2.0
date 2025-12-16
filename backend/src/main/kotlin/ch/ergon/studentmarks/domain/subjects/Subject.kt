package ch.ergon.studentmarks.domain.subjects

import ch.ergon.studentmarks.domain.marks.SubjectId

data class Subject(
    val id: SubjectId,
    val name: String,
    val isVocationalModule: Boolean,
    val description: String?,
)
