package ch.ergon.studentmarks.infrastructure.repository

import ch.ergon.studentmarks.domain.model.subject.Subject
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.repository.SubjectRepository
import ch.ergon.studentmarks.gen.Tables.SUBJECT
import ch.ergon.studentmarks.infrastructure.repository.mapper.SubjectRecordMapper.toSubject
import ch.ergon.studentmarks.infrastructure.security.exeptions.SubjectNotFoundException
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class JooqSubjectRepository(
    private val dsl: DSLContext,
) : SubjectRepository {
    override fun findAll(): List<Subject> =
        dsl
            .selectFrom(SUBJECT)
            .orderBy(SUBJECT.NAME)
            .fetch()
            .map { it.toSubject() }

    override fun findById(subjectId: SubjectId): Subject =
        dsl
            .selectFrom(SUBJECT)
            .where(SUBJECT.ID.eq(subjectId.value))
            .fetchOne()
            ?.toSubject()
            ?: throw SubjectNotFoundException(subjectId)

    override fun save(subject: Subject): Subject =
        dsl
            .insertInto(SUBJECT)
            .set(SUBJECT.ID, subject.id.value)
            .set(SUBJECT.NAME, subject.name)
            .set(SUBJECT.IS_VOCATIONAL_MODULE, subject.isVocationalModule)
            .set(SUBJECT.DESCRIPTION, subject.description)
            .returning()
            .fetchOne()!!
            .toSubject()

    override fun update(subject: Subject): Subject =
        dsl
            .update(SUBJECT)
            .set(SUBJECT.NAME, subject.name)
            .set(SUBJECT.DESCRIPTION, subject.description)
            .set(SUBJECT.IS_VOCATIONAL_MODULE, subject.isVocationalModule)
            .where(SUBJECT.ID.eq(subject.id.value))
            .returning()
            .fetchOne()!!
            .toSubject()
}
