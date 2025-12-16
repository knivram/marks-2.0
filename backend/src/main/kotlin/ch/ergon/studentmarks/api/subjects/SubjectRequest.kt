package ch.ergon.studentmarks.api.subjects

data class SubjectRequest(
    val name: String,
    val isVocationalModule: Boolean,
    val description: String?,
)
