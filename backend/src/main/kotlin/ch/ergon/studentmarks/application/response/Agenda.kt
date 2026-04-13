package ch.ergon.studentmarks.application.response

data class Agenda(
    val pastExamsWithoutGrade: List<MarkWithSubject>,
    val upcomingExams: List<MarkWithSubject>,
)
