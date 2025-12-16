package ch.ergon.studentmarks.domain.semester

import ch.ergon.studentmarks.domain.marks.SemesterId
import java.time.LocalDate

data class Semester(
    val id: SemesterId,
    val semesterName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
