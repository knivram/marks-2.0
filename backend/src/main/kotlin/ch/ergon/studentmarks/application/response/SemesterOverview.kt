package ch.ergon.studentmarks.application.response

data class SemesterOverview(
    val vocationalSubjects: List<SubjectWithMarks>,
    val generalSubjects: List<SubjectWithMarks>,
    val vocationalAverage: Double?,
    val generalAverage: Double?,
)
