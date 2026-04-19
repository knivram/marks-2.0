package ch.ergon.studentmarks.utils

import ch.ergon.studentmarks.api.generated.dto.MarkDto
import ch.ergon.studentmarks.api.generated.dto.MarkRequestDto
import ch.ergon.studentmarks.api.generated.dto.SemesterRequestDto
import ch.ergon.studentmarks.api.generated.dto.SubjectRequestDto
import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.semester.Semester
import ch.ergon.studentmarks.domain.model.subject.Subject
import ch.ergon.studentmarks.test.objects.TestSubjects

fun Semester.toAddSemesterRequest(): SemesterRequestDto =
    SemesterRequestDto(
        semesterName = semesterName,
        startDate = startDate,
        endDate = endDate,
    )

fun Subject.toRequest(): SubjectRequestDto =
    SubjectRequestDto(
        name = name,
        isVocationalModule = isVocationalModule,
        description = description,
    )

private val subjectLookup = TestSubjects.subjects.associateBy { it.id }

/**
 * Test helper: converts a Mark domain object to a MarkDto by resolving the subject from TestSubjects.
 */
fun Mark.toDto(): MarkDto {
    val subject = subjectLookup.getValue(subjectId)
    return MarkDto(
        id = id.value,
        name = name,
        subject = subject.toDto(),
        date = date,
        weight = weight,
        value = value.value,
        semesterId = semesterId.value,
    )
}

/**
 * Test helper: converts a Mark domain object to a MarkRequestDto (for POST/create requests).
 */
fun Mark.toMarkRequestDto(): MarkRequestDto =
    MarkRequestDto(
        name = name,
        subjectId = subjectId.value,
        date = date,
        weight = weight,
        value = value.value,
        semesterId = semesterId.value,
    )
