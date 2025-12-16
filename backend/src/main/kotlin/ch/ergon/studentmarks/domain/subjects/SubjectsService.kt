package ch.ergon.studentmarks.domain.subjects

import ch.ergon.studentmarks.db.SubjectsRepository
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.marks.SubjectId
import ch.ergon.studentmarks.domain.user.UserId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class SubjectsService(
    private val subjectsRepository: SubjectsRepository,
) {
    @PreAuthorize("hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')")
    fun getSubjectOfCurrentUser(
        subjectId: SubjectId,
        semesterId: SemesterId,
        userId: UserId,
    ): SubjectOverview = subjectsRepository.getSubject(subjectId, userId, semesterId)

    @PreAuthorize("hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')")
    fun getAllSubjectsOfCurrentUser(
        semesterId: SemesterId,
        userId: UserId,
    ): List<SubjectOverview> = subjectsRepository.getAllSubjectsOfUser(userId, semesterId)

    fun getAllSubject(): List<Subject> = subjectsRepository.getAllSubjects()

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun editSubject(subject: Subject): Subject = subjectsRepository.editSubject(subject)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun addSubject(subject: Subject): Subject = subjectsRepository.addSubject(subject)
}
