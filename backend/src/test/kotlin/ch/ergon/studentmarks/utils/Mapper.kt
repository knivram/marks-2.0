package ch.ergon.studentmarks.utils

import ch.ergon.studentmarks.api.generated.dto.SemesterRequestDto
import ch.ergon.studentmarks.api.generated.dto.SubjectRequestDto
import ch.ergon.studentmarks.domain.semester.Semester
import ch.ergon.studentmarks.domain.subjects.Subject
import java.time.format.DateTimeFormatter

fun Semester.toAddSemesterRequest(): SemesterRequestDto =
    SemesterRequestDto(
        semesterName = semesterName,
        startDate = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        endDate = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
    )

fun Subject.toRequest(): SubjectRequestDto =
    SubjectRequestDto(
        name = name,
        isVocationalModule = isVocationalModule,
        description = description,
    )
