package ch.ergon.studentmarks.application.query.model

import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.subject.Subject

data class SubjectWithMarks(
    val subject: Subject,
    val average: Double?,
    val marks: List<Mark>,
)
