package ch.ergon.studentmarks.api.subjects

import ch.ergon.studentmarks.api.toDomain
import ch.ergon.studentmarks.api.toDto
import ch.ergon.studentmarks.domain.marks.MarkService
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.marks.SubjectId
import ch.ergon.studentmarks.domain.subjects.Subject
import ch.ergon.studentmarks.domain.subjects.SubjectsService
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.utils.UuidUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/subjects")
class SubjectsController(
    private val subjectsService: SubjectsService,
    private val markService: MarkService,
    private val uuidUtils: UuidUtils,
) {
    @GetMapping("")
    fun getAllSubjectsForCurrentUser(
        @RequestParam semesterId: SemesterId,
        @RequestParam userId: UserId,
    ): List<SubjectOverviewDto> = subjectsService.getAllSubjectsOfCurrentUser(semesterId, userId).map { it.toDto() }

    @PostMapping("")
    fun addSubject(
        @RequestBody subjectRequest: SubjectRequest,
    ): Subject = subjectsService.addSubject(subjectRequest.toDomain(SubjectId(uuidUtils.randomUUID())))

    @PutMapping("/{id}")
    fun editSubject(
        @PathVariable(name = "id") subjectId: SubjectId,
        @RequestBody subjectRequest: SubjectRequest,
    ): Subject {
        val subject = subjectRequest.toDomain(subjectId)
        return subjectsService.editSubject(subject)
    }

    @GetMapping("all")
    fun getAllSubject(): List<Subject> = subjectsService.getAllSubject()

    @GetMapping("/{subjectId}/full")
    fun getFullSubject(
        @PathVariable subjectId: SubjectId,
        @RequestParam semesterId: SemesterId,
        @RequestParam userId: UserId,
    ): FullSubjectDto {
        val markList = markService.getAllMarks(semesterId, userId, subjectId)
        val subject = subjectsService.getSubjectOfCurrentUser(subjectId, semesterId, userId)
        return FullSubjectDto(
            subject.subjectId.value,
            subject.name,
            subject.isVocationalModule,
            subject.description,
            subject.markAverage.value,
            markList,
        )
    }
}
