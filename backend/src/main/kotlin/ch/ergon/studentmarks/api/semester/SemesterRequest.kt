package ch.ergon.studentmarks.api.semester

data class SemesterRequest(
    val semesterName: String,
    val startDate: String,
    val endDate: String,
)
