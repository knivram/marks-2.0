package ch.ergon.studentmarks.api.marks

import ch.ergon.studentmarks.domain.marks.Mark
import ch.ergon.studentmarks.domain.marks.MarkId
import ch.ergon.studentmarks.domain.marks.MarkService
import ch.ergon.studentmarks.domain.marks.MarkValue
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.marks.SubjectId
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.utils.UuidUtils
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/marks")
class MarksController(
    private val markService: MarkService,
    private val uuidUtils: UuidUtils,
) {
    @GetMapping("")
    fun getAllMarks(
        @RequestParam(required = false) subjectId: SubjectId?,
        @RequestParam semesterId: SemesterId,
        @RequestParam userId: UserId,
    ): List<Mark> = markService.getAllMarks(semesterId, userId, subjectId)

    @PostMapping("")
    fun addMark(
        @RequestBody requestMark: Mark,
        @RequestParam semesterId: SemesterId,
    ): Mark {
        val mark =
            Mark(
                MarkId(uuidUtils.randomUUID()),
                requestMark.name,
                requestMark.subject,
                requestMark.date,
                requestMark.weight,
                requestMark.value,
                semesterId,
            )
        return markService.addNewMark(mark)
    }

    @GetMapping("future-exams")
    fun getFutureExams(
        @RequestParam userId: UserId,
    ): List<Mark> = markService.getFutureExams(userId)

    @GetMapping("passed-exams")
    fun getPassedExams(
        @RequestParam userId: UserId,
    ): List<Mark> = markService.getPassedExams(userId)

    @GetMapping("/last")
    fun lastMarks(
        @RequestParam userId: UserId,
        @RequestParam(required = false, defaultValue = "4") limit: Int,
    ): List<Mark> = markService.getPassedMarksWithValue(userId, limit)

    @PostMapping("{markId}/edit")
    fun editMark(
        @PathVariable markId: MarkId,
        @RequestBody mark: Mark,
    ): Mark = markService.editMark(mark)

    @DeleteMapping("{markId}/delete")
    fun deleteMark(
        @PathVariable markId: MarkId,
    ): MarkId {
        markService.deleteMark(markId)
        return markId
    }

    @PostMapping("{markId}/restore")
    fun restoreMark(
        @PathVariable markId: MarkId,
    ): Mark = markService.restoreMark(markId)

    @GetMapping("/calculate")
    fun calculator(
        @RequestParam userId: UserId,
        @RequestParam semesterId: SemesterId,
        @RequestParam subjectId: SubjectId,
        @RequestParam goalMark: Int,
        @RequestParam examWeight: Int,
    ): MarkValue = markService.calculate(userId, semesterId, subjectId, MarkValue(goalMark), examWeight)
}
