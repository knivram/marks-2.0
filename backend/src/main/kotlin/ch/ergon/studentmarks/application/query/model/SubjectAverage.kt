package ch.ergon.studentmarks.application.query.model

import ch.ergon.studentmarks.domain.model.subject.Subject

data class SubjectAverage(
    val subject: Subject,
    val average: Double?,
)
