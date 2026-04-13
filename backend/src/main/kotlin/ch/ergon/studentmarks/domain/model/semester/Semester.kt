package ch.ergon.studentmarks.domain.model.semester

import java.time.LocalDate

data class Semester(
    val id: SemesterId,
    val semesterName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
