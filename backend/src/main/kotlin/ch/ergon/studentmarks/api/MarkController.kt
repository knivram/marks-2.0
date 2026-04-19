package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.api.generated.MarksApi
import ch.ergon.studentmarks.api.generated.dto.AgendaDto
import ch.ergon.studentmarks.api.generated.dto.DashboardAveragesDto
import ch.ergon.studentmarks.api.generated.dto.MarkDto
import ch.ergon.studentmarks.api.generated.dto.MarkRequestDto
import ch.ergon.studentmarks.api.generated.dto.MarkValueDto
import ch.ergon.studentmarks.api.generated.dto.SemesterOverviewDto
import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.application.service.MarkService
import ch.ergon.studentmarks.application.service.UserService
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.UserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class MarkController(
    private val markService: MarkService,
    private val userService: UserService,
) : MarksApi {
    override fun addMark(body: MarkRequestDto): ResponseEntity<MarkDto> {
        val result =
            markService.addNewMark(
                name = body.name,
                subjectId = SubjectId(body.subjectId),
                date = body.date,
                weight = body.weight,
                value = MarkValue(body.value),
                semesterId = SemesterId(body.semesterId),
            )
        return ResponseEntity.ok(result.toDto())
    }

    override fun editMark(
        markId: UUID,
        body: MarkRequestDto,
    ): ResponseEntity<MarkDto> {
        val result =
            markService.editMark(
                markId = MarkId(markId),
                name = body.name,
                subjectId = SubjectId(body.subjectId),
                date = body.date,
                weight = body.weight,
                value = MarkValue(body.value),
                semesterId = SemesterId(body.semesterId),
            )
        return ResponseEntity.ok(result.toDto())
    }

    override fun deleteMark(markId: UUID): ResponseEntity<UUID> {
        markService.deleteMark(MarkId(markId))
        return ResponseEntity.ok(markId)
    }

    override fun restoreMark(markId: UUID): ResponseEntity<MarkDto> {
        val result = markService.restoreMark(MarkId(markId))
        return ResponseEntity.ok(result.toDto())
    }

    override fun calculator(
        semesterId: UUID,
        subjectId: UUID,
        goalMark: Int,
        examWeight: Int,
        userId: UUID?,
    ): ResponseEntity<MarkValueDto> {
        val resolvedUserId = userService.resolveUserId(userId?.let { UserId(it) })
        val result =
            markService.calculate(
                resolvedUserId,
                SemesterId(semesterId),
                SubjectId(subjectId),
                MarkValue(goalMark),
                examWeight,
            )
        return ResponseEntity.ok(result.toDto())
    }

    override fun getAgenda(userId: UUID?): ResponseEntity<AgendaDto> {
        val resolvedUserId = userService.resolveUserId(userId?.let { UserId(it) })
        val agenda = markService.getAgenda(resolvedUserId)
        return ResponseEntity.ok(agenda.toDto())
    }

    override fun getSemesterOverview(
        semesterId: UUID,
        userId: UUID?,
    ): ResponseEntity<SemesterOverviewDto> {
        val resolvedUserId = userService.resolveUserId(userId?.let { UserId(it) })
        val overview = markService.getSemesterOverview(resolvedUserId, SemesterId(semesterId))
        return ResponseEntity.ok(overview.toDto())
    }

    override fun getSemesterAverages(
        semesterId: UUID,
        userId: UUID?,
    ): ResponseEntity<DashboardAveragesDto> {
        val resolvedUserId = userService.resolveUserId(userId?.let { UserId(it) })
        val averages = markService.getDashboardAverages(resolvedUserId, SemesterId(semesterId))
        return ResponseEntity.ok(averages.toDto())
    }
}
