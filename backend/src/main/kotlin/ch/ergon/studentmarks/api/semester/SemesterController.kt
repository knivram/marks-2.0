package ch.ergon.studentmarks.api.semester

import ch.ergon.studentmarks.api.generated.SemestersApi
import ch.ergon.studentmarks.api.generated.dto.PaginatedSemesterListDto
import ch.ergon.studentmarks.api.generated.dto.SemesterDto
import ch.ergon.studentmarks.api.generated.dto.SemesterRequestDto
import ch.ergon.studentmarks.api.toDomain
import ch.ergon.studentmarks.api.toDto
import ch.ergon.studentmarks.api.toPaginatedSemesterListDto
import ch.ergon.studentmarks.domain.PaginationParams
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.semester.SemesterService
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.utils.UuidUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class SemesterController(
    private val semesterService: SemesterService,
    private val uuidUtils: UuidUtils,
) : SemestersApi {
    override fun getAllSemester(offset: Int, limit: Int): ResponseEntity<PaginatedSemesterListDto> {
        val pagedSemesterList = semesterService.getAllSemester(PaginationParams(offset, limit))
        return ResponseEntity.ok(pagedSemesterList.toPaginatedSemesterListDto(offset))
    }

    override fun addSemester(body: SemesterRequestDto): ResponseEntity<SemesterDto> =
        ResponseEntity.ok(
            semesterService.addSemester(body.toDomain(SemesterId(uuidUtils.randomUUID()))).toDto(),
        )

    override fun editSemester(id: UUID, body: SemesterRequestDto): ResponseEntity<SemesterDto> =
        ResponseEntity.ok(
            semesterService.editSemester(body.toDomain(SemesterId(id))).toDto(),
        )

    override fun getRelevantSemester(userId: UUID): ResponseEntity<List<SemesterDto>> =
        ResponseEntity.ok(semesterService.getAllRelevantSemester(UserId(userId)).map { it.toDto() })
}
