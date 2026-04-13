package ch.ergon.studentmarks.domain.model.subject

data class Subject(
    val id: SubjectId,
    val name: String,
    val isVocationalModule: Boolean,
    val description: String?,
)
