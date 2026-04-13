package ch.ergon.studentmarks.api.student

import ch.ergon.studentmarks.api.generated.StudentMarksApi
import ch.ergon.studentmarks.api.generated.UserInfoApi
import ch.ergon.studentmarks.api.generated.dto.MarkDto
import ch.ergon.studentmarks.api.generated.dto.MarkRequestDto
import ch.ergon.studentmarks.api.generated.dto.MarkValueDto
import ch.ergon.studentmarks.api.generated.dto.MarkWithSubjectDto
import ch.ergon.studentmarks.api.generated.dto.UserDto
import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.application.query.model.MarkWithSubject
import ch.ergon.studentmarks.application.service.MarkService
import ch.ergon.studentmarks.application.service.UserService
import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.SubjectRepository
import ch.ergon.studentmarks.utils.UuidUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class MarkController(
    private val markService: MarkService,
    private val userService: UserService,
    private val subjectRepository: SubjectRepository,
    private val uuidUtils: UuidUtils,
) : StudentMarksApi,
    UserInfoApi {
    override fun addMark(
        semesterId: UUID,
        body: MarkRequestDto,
    ): ResponseEntity<MarkWithSubjectDto> {
        val mark =
            Mark(
                id = MarkId(uuidUtils.randomUUID()),
                name = body.name,
                subjectId = SubjectId(body.subjectId),
                date = body.date,
                weight = body.weight,
                value = MarkValue(body.value),
                semesterId = SemesterId(semesterId),
            )
        val savedMark = markService.addNewMark(mark)
        val subject = subjectRepository.findById(savedMark.subjectId)
        return ResponseEntity.ok(MarkWithSubject(savedMark, subject).toDto())
    }

    override fun editMark(
        markId: UUID,
        markDto: MarkDto,
    ): ResponseEntity<MarkWithSubjectDto> {
        val mark =
            Mark(
                id = MarkId(markId),
                name = markDto.name,
                subjectId = SubjectId(markDto.subject.id),
                date = markDto.date,
                weight = markDto.weight,
                value = MarkValue(markDto.value),
                semesterId = SemesterId(markDto.semesterId),
            )
        val updatedMark = markService.editMark(mark)
        val subject = subjectRepository.findById(updatedMark.subjectId)
        return ResponseEntity.ok(MarkWithSubject(updatedMark, subject).toDto())
    }

    override fun deleteMark(markId: UUID): ResponseEntity<UUID> {
        markService.deleteMark(MarkId(markId))
        return ResponseEntity.ok(markId)
    }

    override fun restoreMark(markId: UUID): ResponseEntity<MarkWithSubjectDto> {
        val restoredMark = markService.restoreMark(MarkId(markId))
        val subject = subjectRepository.findById(restoredMark.subjectId)
        return ResponseEntity.ok(MarkWithSubject(restoredMark, subject).toDto())
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

    override fun userInfo(): ResponseEntity<UserDto> {
        val user = userService.getCurrentUser()
        return ResponseEntity.ok(user.toDto())
    }

    override fun getApprentice(id: UUID): ResponseEntity<List<UserDto>> {
        val apprentices = userService.getApprentice(UserId(id))
        return ResponseEntity.ok(apprentices.map { it.toDto() })
    }
}
