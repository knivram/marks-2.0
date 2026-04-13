package ch.ergon.studentmarks.application.query.model

data class Agenda(
    val pastExamsWithoutGrade: List<MarkWithSubject>,
    val upcomingExams: List<MarkWithSubject>,
)
