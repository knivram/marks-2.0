package ch.ergon.studentmarks.api.semester

import ch.ergon.studentmarks.api.toDomain
import ch.ergon.studentmarks.api.toDto
import ch.ergon.studentmarks.domain.PaginationParams
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.semester.Semester
import ch.ergon.studentmarks.domain.semester.SemesterService
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
@RequestMapping("/api/semesters")
class SemesterController(
    private val semesterService: SemesterService,
    private val uuidUtils: UuidUtils,
) {
    @GetMapping("")
    fun getAllSemester(
        @RequestParam(required = false, defaultValue = "0") offset: Number,
        @RequestParam(required = false, defaultValue = "20") limit: Number,
    ): PaginatedListDto<Semester> {
        val pagedSemesterList = semesterService.getAllSemester(PaginationParams(offset, limit))

        return pagedSemesterList.toDto(offset)
    }

    @PostMapping("")
    fun addSemester(
        @RequestBody semesterRequest: SemesterRequest,
    ): Semester = semesterService.addSemester(semesterRequest.toDomain(SemesterId(uuidUtils.randomUUID())))

    @PutMapping("/{id}")
    fun editSemester(
        @RequestBody semesterRequest: SemesterRequest,
        @PathVariable(name = "id") semesterId: SemesterId,
    ): Semester = semesterService.editSemester(semesterRequest.toDomain(semesterId))

    @GetMapping("/relevant")
    fun getRelevantSemester(
        @RequestParam userId: UserId,
    ): List<Semester> = semesterService.getAllRelevantSemester(userId)
}
