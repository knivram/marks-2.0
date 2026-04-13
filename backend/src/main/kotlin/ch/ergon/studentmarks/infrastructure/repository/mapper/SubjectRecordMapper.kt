package ch.ergon.studentmarks.infrastructure.repository.mapper

import ch.ergon.studentmarks.domain.model.subject.Subject
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.gen.Tables.SUBJECT
import org.jooq.Record

object SubjectRecordMapper {
    fun Record.toSubject(): Subject =
        Subject(
            id = SubjectId(this[SUBJECT.ID]),
            name = this[SUBJECT.NAME],
            isVocationalModule = this[SUBJECT.IS_VOCATIONAL_MODULE],
            description = this[SUBJECT.DESCRIPTION],
        )
}
