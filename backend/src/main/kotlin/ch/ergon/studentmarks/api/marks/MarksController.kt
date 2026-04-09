package ch.ergon.studentmarks.api.marks

import ch.ergon.studentmarks.api.generated.MarksApi
import ch.ergon.studentmarks.api.generated.dto.MarkDto
import ch.ergon.studentmarks.api.generated.dto.MarkRequestDto
import ch.ergon.studentmarks.api.generated.dto.MarkValueDto
import ch.ergon.studentmarks.api.toDto
import ch.ergon.studentmarks.domain.marks.Mark
import ch.ergon.studentmarks.domain.marks.MarkId
import ch.ergon.studentmarks.domain.marks.MarkService
import ch.ergon.studentmarks.domain.marks.MarkValue
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.marks.SubjectId
import ch.ergon.studentmarks.domain.subjects.Subject
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.utils.UuidUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class MarksController(
    private val markService: MarkService,
    private val uuidUtils: UuidUtils,
) : MarksApi {
    override fun getAllMarks(
        semesterId: UUID,
        userId: UUID,
        subjectId: UUID?,
    ): ResponseEntity<List<MarkDto>> =
        ResponseEntity.ok(
            markService
                .getAllMarks(
                    SemesterId(semesterId),
                    UserId(userId),
                    subjectId?.let { SubjectId(it) },
                ).map { it.toDto() },
        )

    override fun addMark(
        semesterId: UUID,
        body: MarkRequestDto,
    ): ResponseEntity<MarkDto> {
        val mark =
            Mark(
                MarkId(uuidUtils.randomUUID()),
                body.name,
                Subject(
                    SubjectId(body.subject.id),
                    body.subject.name,
                    body.subject.isVocationalModule,
                    body.subject.description,
                ),
                body.date,
                body.weight,
                MarkValue(body.value),
                SemesterId(semesterId),
            )
        return ResponseEntity.ok(markService.addNewMark(mark).toDto())
    }

    override fun getFutureExams(userId: UUID): ResponseEntity<List<MarkDto>> =
        ResponseEntity.ok(markService.getFutureExams(UserId(userId)).map { it.toDto() })

    override fun getPassedExams(userId: UUID): ResponseEntity<List<MarkDto>> =
        ResponseEntity.ok(markService.getPassedExams(UserId(userId)).map { it.toDto() })

    override fun lastMarks(
        userId: UUID,
        limit: Int,
    ): ResponseEntity<List<MarkDto>> = ResponseEntity.ok(markService.getPassedMarksWithValue(UserId(userId), limit).map { it.toDto() })

    override fun editMark(
        markId: UUID,
        markDto: MarkDto,
    ): ResponseEntity<MarkDto> {
        val mark =
            Mark(
                MarkId(markId),
                markDto.name,
                Subject(
                    SubjectId(markDto.subject.id),
                    markDto.subject.name,
                    markDto.subject.isVocationalModule,
                    markDto.subject.description,
                ),
                markDto.date,
                markDto.weight,
                MarkValue(markDto.value),
                SemesterId(markDto.semesterId),
            )
        return ResponseEntity.ok(markService.editMark(mark).toDto())
    }

    override fun deleteMark(markId: UUID): ResponseEntity<UUID> {
        markService.deleteMark(MarkId(markId))
        return ResponseEntity.ok(markId)
    }

    override fun restoreMark(markId: UUID): ResponseEntity<MarkDto> = ResponseEntity.ok(markService.restoreMark(MarkId(markId)).toDto())

    override fun calculator(
        userId: UUID,
        semesterId: UUID,
        subjectId: UUID,
        goalMark: Int,
        examWeight: Int,
    ): ResponseEntity<MarkValueDto> =
        ResponseEntity.ok(
            markService
                .calculate(
                    UserId(userId),
                    SemesterId(semesterId),
                    SubjectId(subjectId),
                    MarkValue(goalMark),
                    examWeight,
                ).toDto(),
        )
}
