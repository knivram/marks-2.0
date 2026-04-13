package ch.ergon.studentmarks.application.service

import ch.ergon.studentmarks.domain.model.subject.Subject
import ch.ergon.studentmarks.domain.repository.SubjectRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class SubjectService(
    private val subjectRepository: SubjectRepository,
) {
    fun getAllSubjects(): List<Subject> = subjectRepository.findAll()

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun editSubject(subject: Subject): Subject = subjectRepository.update(subject)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun addSubject(subject: Subject): Subject = subjectRepository.save(subject)
}
