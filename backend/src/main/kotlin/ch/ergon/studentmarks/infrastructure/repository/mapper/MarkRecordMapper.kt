package ch.ergon.studentmarks.infrastructure.repository.mapper

import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.gen.Tables.MARK
import org.jooq.Record

object MarkRecordMapper {
    fun Record.toMark(): Mark =
        Mark(
            id = MarkId(this[MARK.ID]),
            name = this[MARK.NAME],
            subjectId = SubjectId(this[MARK.SUBJECT_ID]),
            date = this[MARK.DATE],
            weight = this[MARK.WEIGHT],
            value = MarkValue(this[MARK.VALUE]),
            semesterId = SemesterId(this[MARK.SEMESTER_ID]),
        )
}
