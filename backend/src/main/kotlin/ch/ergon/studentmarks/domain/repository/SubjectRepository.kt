package ch.ergon.studentmarks.domain.repository

import ch.ergon.studentmarks.domain.model.subject.Subject
import ch.ergon.studentmarks.domain.model.subject.SubjectId

interface SubjectRepository {
    fun getAll(): List<Subject>

    fun getById(subjectId: SubjectId): Subject

    fun save(subject: Subject): Subject

    fun update(subject: Subject): Subject
}
