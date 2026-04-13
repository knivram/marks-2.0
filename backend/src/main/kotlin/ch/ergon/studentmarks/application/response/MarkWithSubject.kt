package ch.ergon.studentmarks.application.response

import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.subject.Subject

data class MarkWithSubject(
    val mark: Mark,
    val subject: Subject,
)
