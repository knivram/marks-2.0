package ch.ergon.studentmarks.application.query.model

data class SemesterOverview(
    val vocationalSubjects: List<SubjectWithMarks>,
    val generalSubjects: List<SubjectWithMarks>,
    val vocationalAverage: Double?,
    val generalAverage: Double?,
)
