package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.api.generated.SemestersApi
import ch.ergon.studentmarks.api.generated.dto.PaginatedSemesterListDto
import ch.ergon.studentmarks.api.generated.dto.SemesterDto
import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.api.mapper.toPaginatedSemesterListDto
import ch.ergon.studentmarks.application.security.ActingUserProvider
import ch.ergon.studentmarks.application.service.SemesterService
import ch.ergon.studentmarks.domain.model.shared.PaginationParams
import ch.ergon.studentmarks.domain.model.user.UserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class SemesterController(
    private val semesterService: SemesterService,
    private val actingUserProvider: ActingUserProvider,
) : SemestersApi {
    override fun getAllSemesters(
        offset: Int,
        limit: Int,
    ): ResponseEntity<PaginatedSemesterListDto> {
        val paginatedList = semesterService.getAllSemester(PaginationParams(offset, limit))
        return ResponseEntity.ok(paginatedList.toPaginatedSemesterListDto(offset))
    }

    override fun getRelevantSemesters(userId: UUID?): ResponseEntity<List<SemesterDto>> {
        // TODO: Make userId required
        val resolvedUserId = userId?.let { UserId(it) } ?: actingUserProvider.actingPerson.user.userId
        val semesters = semesterService.getAllRelevantSemester(resolvedUserId)
        return ResponseEntity.ok(semesters.map { it.toDto() })
    }
}
