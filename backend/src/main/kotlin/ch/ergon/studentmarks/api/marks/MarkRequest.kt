package ch.ergon.studentmarks.api.marks

import java.time.LocalDate

data class MarkRequest(
    val name: String,
    val date: LocalDate,
    val weight: Int,
    val value: Int,
)
