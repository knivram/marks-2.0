package ch.ergon.studentmarks.application.query

import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.Subject
import java.time.LocalDate

data class MarkView(
    val id: MarkId,
    val name: String,
    val subject: Subject,
    val date: LocalDate,
    val weight: Int,
    val value: MarkValue,
    val semesterId: SemesterId,
) {
    fun toMark(): Mark =
        Mark(
            id = id,
            name = name,
            subjectId = subject.id,
            date = date,
            weight = weight,
            value = value,
            semesterId = semesterId,
        )
}
