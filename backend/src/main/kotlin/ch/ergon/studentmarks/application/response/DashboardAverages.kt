package ch.ergon.studentmarks.application.response

data class DashboardAverages(
    val vocationalSubjects: List<SubjectAverage>,
    val generalSubjects: List<SubjectAverage>,
    val vocationalAverage: Double?,
    val generalAverage: Double?,
)
