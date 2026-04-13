package ch.ergon.studentmarks.domain.model.mark

import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import java.time.LocalDate

data class Mark(
    val id: MarkId,
    val name: String,
    val subjectId: SubjectId,
    val date: LocalDate,
    val weight: Int,
    val value: MarkValue,
    val semesterId: SemesterId,
)
