package ch.ergon.studentmarks.application.response

import ch.ergon.studentmarks.domain.model.subject.Subject

data class SubjectAverage(
    val subject: Subject,
    val average: Double?,
)
