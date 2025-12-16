package ch.ergon.studentmarks.domain.marks

import ch.ergon.studentmarks.domain.subjects.Subject
import java.time.LocalDate

data class Mark(
    val id: MarkId,
    val name: String,
    val subject: Subject,
    val date: LocalDate,
    val weight: Int,
    val value: MarkValue,
    val semesterId: SemesterId,
)
