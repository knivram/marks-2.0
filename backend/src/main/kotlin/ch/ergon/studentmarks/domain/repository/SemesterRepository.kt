package ch.ergon.studentmarks.domain.repository

import ch.ergon.studentmarks.domain.model.semester.Semester
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.shared.PaginatedList
import ch.ergon.studentmarks.domain.model.shared.PaginationParams
import ch.ergon.studentmarks.domain.model.user.UserId
import java.time.LocalDate

interface SemesterRepository {
    fun findAll(paginationParams: PaginationParams): PaginatedList<Semester>

    fun findById(semesterId: SemesterId): Semester

    fun save(semester: Semester): Semester

    fun update(semester: Semester): Semester

    fun findRelevantForUser(
        userId: UserId,
        asOf: LocalDate,
    ): List<Semester>
}
