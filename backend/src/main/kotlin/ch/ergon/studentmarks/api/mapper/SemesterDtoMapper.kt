package ch.ergon.studentmarks.api.mapper

import ch.ergon.studentmarks.api.generated.dto.PaginatedSemesterListDto
import ch.ergon.studentmarks.api.generated.dto.SemesterDto
import ch.ergon.studentmarks.api.generated.dto.SemesterRequestDto
import ch.ergon.studentmarks.domain.model.semester.Semester
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.shared.PaginatedList

fun Semester.toDto(): SemesterDto =
    SemesterDto(
        id = id.value,
        semesterName = semesterName,
        startDate = startDate,
        endDate = endDate,
    )

fun SemesterRequestDto.toDomain(semesterId: SemesterId): Semester =
    Semester(
        id = semesterId,
        semesterName = semesterName,
        startDate = startDate,
        endDate = endDate,
    )

fun PaginatedList<Semester>.toPaginatedSemesterListDto(offset: Int): PaginatedSemesterListDto =
    PaginatedSemesterListDto(
        totalEntries = totalEntries,
        offset = offset,
        list = this.map { it.toDto() },
    )
