package ch.ergon.studentmarks.api.mapper

import ch.ergon.studentmarks.api.generated.dto.SubjectDto
import ch.ergon.studentmarks.api.generated.dto.SubjectRequestDto
import ch.ergon.studentmarks.domain.model.subject.Subject
import ch.ergon.studentmarks.domain.model.subject.SubjectId

fun Subject.toDto(): SubjectDto =
    SubjectDto(
        id = id.value,
        name = name,
        isVocationalModule = isVocationalModule,
        description = description,
    )

fun SubjectRequestDto.toDomain(subjectId: SubjectId): Subject =
    Subject(
        id = subjectId,
        name = name,
        isVocationalModule = isVocationalModule,
        description = description,
    )
