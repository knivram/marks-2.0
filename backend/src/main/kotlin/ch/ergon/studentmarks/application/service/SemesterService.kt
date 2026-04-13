package ch.ergon.studentmarks.application.service

import ch.ergon.studentmarks.domain.model.semester.Semester
import ch.ergon.studentmarks.domain.model.shared.PaginatedList
import ch.ergon.studentmarks.domain.model.shared.PaginationParams
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.SemesterRepository
import ch.ergon.studentmarks.utils.TimeUtils
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class SemesterService(
    private val semesterRepository: SemesterRepository,
    private val timeUtils: TimeUtils,
) {
    fun getAllSemester(paginationParams: PaginationParams): PaginatedList<Semester> = semesterRepository.findAll(paginationParams)

    @PreAuthorize(
        "hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')",
    )
    fun getAllRelevantSemester(userId: UserId): List<Semester> = semesterRepository.findRelevantForUser(userId, timeUtils.now())

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun addSemester(semester: Semester): Semester = semesterRepository.save(semester)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun editSemester(semester: Semester): Semester = semesterRepository.update(semester)
}
