package ch.ergon.studentmarks.api.subjects

import ch.ergon.studentmarks.api.generated.SubjectsApi
import ch.ergon.studentmarks.api.generated.dto.FullSubjectDto
import ch.ergon.studentmarks.api.generated.dto.SubjectDto
import ch.ergon.studentmarks.api.generated.dto.SubjectOverviewDto
import ch.ergon.studentmarks.api.generated.dto.SubjectRequestDto
import ch.ergon.studentmarks.api.toDomain
import ch.ergon.studentmarks.api.toDto
import ch.ergon.studentmarks.domain.marks.MarkService
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.marks.SubjectId
import ch.ergon.studentmarks.domain.subjects.SubjectsService
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.utils.UuidUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class SubjectsController(
    private val subjectsService: SubjectsService,
    private val markService: MarkService,
    private val uuidUtils: UuidUtils,
) : SubjectsApi {
    override fun getAllSubjectsForCurrentUser(
        semesterId: UUID,
        userId: UUID,
    ): ResponseEntity<List<SubjectOverviewDto>> =
        ResponseEntity.ok(
            subjectsService.getAllSubjectsOfCurrentUser(SemesterId(semesterId), UserId(userId)).map { it.toDto() },
        )

    override fun addSubject(body: SubjectRequestDto): ResponseEntity<SubjectDto> =
        ResponseEntity.ok(
            subjectsService.addSubject(body.toDomain(SubjectId(uuidUtils.randomUUID()))).toDto(),
        )

    override fun editSubject(
        id: UUID,
        body: SubjectRequestDto,
    ): ResponseEntity<SubjectDto> =
        ResponseEntity.ok(
            subjectsService.editSubject(body.toDomain(SubjectId(id))).toDto(),
        )

    override fun getAllSubject(): ResponseEntity<List<SubjectDto>> = ResponseEntity.ok(subjectsService.getAllSubject().map { it.toDto() })

    override fun getFullSubject(
        subjectId: UUID,
        semesterId: UUID,
        userId: UUID,
    ): ResponseEntity<FullSubjectDto> {
        val markList = markService.getAllMarks(SemesterId(semesterId), UserId(userId), SubjectId(subjectId))
        val subject = subjectsService.getSubjectOfCurrentUser(SubjectId(subjectId), SemesterId(semesterId), UserId(userId))
        return ResponseEntity.ok(
            FullSubjectDto(
                subjectId = subject.subjectId.value,
                subjectName = subject.name,
                isVocationalModule = subject.isVocationalModule,
                description = subject.description,
                markAverage = subject.markAverage.value,
                marks = markList.map { it.toDto() },
            ),
        )
    }
}
