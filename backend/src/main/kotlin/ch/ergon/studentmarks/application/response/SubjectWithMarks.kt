package ch.ergon.studentmarks.application.response

import ch.ergon.studentmarks.application.query.MarkView
import ch.ergon.studentmarks.domain.model.subject.Subject

data class SubjectWithMarks(
    val subject: Subject,
    val average: Double?,
    val marks: List<MarkView>,
)
