package ch.ergon.studentmarks.api.student

import ch.ergon.studentmarks.api.generated.AgendaApi
import ch.ergon.studentmarks.api.generated.StudentSemestersApi
import ch.ergon.studentmarks.api.generated.dto.AgendaDto
import ch.ergon.studentmarks.api.generated.dto.DashboardAveragesDto
import ch.ergon.studentmarks.api.generated.dto.PaginatedSemesterListDto
import ch.ergon.studentmarks.api.generated.dto.SemesterDto
import ch.ergon.studentmarks.api.generated.dto.SemesterOverviewDto
import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.api.mapper.toPaginatedSemesterListDto
import ch.ergon.studentmarks.application.query.AgendaQueryService
import ch.ergon.studentmarks.application.query.SemesterOverviewQueryService
import ch.ergon.studentmarks.application.service.SemesterService
import ch.ergon.studentmarks.application.service.UserService
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.shared.PaginationParams
import ch.ergon.studentmarks.domain.model.user.UserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class SemesterController(
    private val semesterService: SemesterService,
    private val overviewQueryService: SemesterOverviewQueryService,
    private val agendaQueryService: AgendaQueryService,
    private val userService: UserService,
) : StudentSemestersApi,
    AgendaApi {
    override fun getAllSemesters(
        offset: Int,
        limit: Int,
    ): ResponseEntity<PaginatedSemesterListDto> {
        val paginatedList = semesterService.getAllSemester(PaginationParams(offset, limit))
        return ResponseEntity.ok(paginatedList.toPaginatedSemesterListDto(offset))
    }

    override fun getRelevantSemesters(userId: UUID?): ResponseEntity<List<SemesterDto>> {
        val resolvedUserId = userService.resolveUserId(userId?.let { UserId(it) })
        val semesters = semesterService.getAllRelevantSemester(resolvedUserId)
        return ResponseEntity.ok(semesters.map { it.toDto() })
    }

    override fun getSemesterOverview(
        semesterId: UUID,
        userId: UUID?,
    ): ResponseEntity<SemesterOverviewDto> {
        val resolvedUserId = userService.resolveUserId(userId?.let { UserId(it) })
        val overview = overviewQueryService.getSemesterOverview(resolvedUserId, SemesterId(semesterId))
        return ResponseEntity.ok(overview.toDto())
    }

    override fun getSemesterAverages(
        semesterId: UUID,
        userId: UUID?,
    ): ResponseEntity<DashboardAveragesDto> {
        val resolvedUserId = userService.resolveUserId(userId?.let { UserId(it) })
        val averages = overviewQueryService.getDashboardAverages(resolvedUserId, SemesterId(semesterId))
        return ResponseEntity.ok(averages.toDto())
    }

    override fun getAgenda(userId: UUID?): ResponseEntity<AgendaDto> {
        val resolvedUserId = userService.resolveUserId(userId?.let { UserId(it) })
        val agenda = agendaQueryService.getAgenda(resolvedUserId)
        return ResponseEntity.ok(agenda.toDto())
    }
}
