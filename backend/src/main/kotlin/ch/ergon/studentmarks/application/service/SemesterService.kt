package ch.ergon.studentmarks.application.service

import ch.ergon.studentmarks.application.security.HasStudentDataReadPermission
import ch.ergon.studentmarks.application.security.IsAdmin
import ch.ergon.studentmarks.application.security.NoPermissionRequired
import ch.ergon.studentmarks.application.security.TargetId
import ch.ergon.studentmarks.domain.model.semester.Semester
import ch.ergon.studentmarks.domain.model.shared.PaginatedList
import ch.ergon.studentmarks.domain.model.shared.PaginationParams
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.SemesterRepository
import ch.ergon.studentmarks.utils.TimeUtils
import org.springframework.stereotype.Service

@Service
class SemesterService(
    private val semesterRepository: SemesterRepository,
    // TODO: Replace with spring clock
    private val timeUtils: TimeUtils,
) {
    @NoPermissionRequired("Semesters are reference data every user needs")
    fun getAllSemester(paginationParams: PaginationParams): PaginatedList<Semester> = semesterRepository.getAll(paginationParams)

    @HasStudentDataReadPermission
    fun getAllRelevantSemester(
        @TargetId userId: UserId,
    ): List<Semester> = semesterRepository.findRelevantForUser(userId, timeUtils.now())

    @IsAdmin
    fun addSemester(semester: Semester): Semester = semesterRepository.save(semester)

    @IsAdmin
    fun editSemester(semester: Semester): Semester = semesterRepository.update(semester)
}
