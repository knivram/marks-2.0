package ch.ergon.studentmarks.application.response

import ch.ergon.studentmarks.application.query.MarkView

data class Agenda(
    val pastExamsWithoutGrade: List<MarkView>,
    val upcomingExams: List<MarkView>,
)
