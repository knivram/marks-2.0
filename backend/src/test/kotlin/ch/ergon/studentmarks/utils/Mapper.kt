package ch.ergon.studentmarks.utils

import ch.ergon.studentmarks.api.semester.SemesterRequest
import ch.ergon.studentmarks.api.subjects.SubjectRequest
import ch.ergon.studentmarks.domain.semester.Semester
import ch.ergon.studentmarks.domain.subjects.Subject
import java.time.format.DateTimeFormatter

fun Semester.toAddSemesterRequest(): SemesterRequest =
    SemesterRequest(
        semesterName = semesterName,
        startDate = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        endDate = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
    )

fun Subject.toRequest(): SubjectRequest =
    SubjectRequest(
        name = name,
        isVocationalModule = isVocationalModule,
        description = description,
    )
