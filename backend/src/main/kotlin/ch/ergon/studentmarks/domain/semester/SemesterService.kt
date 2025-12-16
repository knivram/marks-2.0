package ch.ergon.studentmarks.domain.semester

import ch.ergon.studentmarks.db.SemesterRepository
import ch.ergon.studentmarks.domain.PaginatedList
import ch.ergon.studentmarks.domain.PaginationParams
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.utils.TimeUtils
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class SemesterService(
    private val semesterRepository: SemesterRepository,
    private val timeUtils: TimeUtils,
) {
    fun getAllSemester(paginationParams: PaginationParams): PaginatedList<Semester> =
        PaginatedList(
            semesterRepository.getAmountOfSemesters(),
            semesterRepository.getAllSemester(paginationParams),
        )

    @PreAuthorize("hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')")
    fun getAllRelevantSemester(userId: UserId): List<Semester> = semesterRepository.getAllRelevantSemester(userId, timeUtils.now())

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun addSemester(semester: Semester): Semester = semesterRepository.addSemester(semester)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun editSemester(semester: Semester): Semester = semesterRepository.editSemester(semester)
}
