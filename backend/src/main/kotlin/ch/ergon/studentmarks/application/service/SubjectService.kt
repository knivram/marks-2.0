package ch.ergon.studentmarks.application.service

import ch.ergon.studentmarks.application.security.IsAdmin
import ch.ergon.studentmarks.application.security.NoPermissionRequired
import ch.ergon.studentmarks.domain.model.subject.Subject
import ch.ergon.studentmarks.domain.repository.SubjectRepository
import org.springframework.stereotype.Service

@Service
class SubjectService(
    private val subjectRepository: SubjectRepository,
) {
    @NoPermissionRequired("Subjects are reference data every user needs")
    fun getAllSubjects(): List<Subject> = subjectRepository.getAll()

    @IsAdmin
    fun editSubject(subject: Subject): Subject = subjectRepository.update(subject)

    @IsAdmin
    fun addSubject(subject: Subject): Subject = subjectRepository.save(subject)
}
