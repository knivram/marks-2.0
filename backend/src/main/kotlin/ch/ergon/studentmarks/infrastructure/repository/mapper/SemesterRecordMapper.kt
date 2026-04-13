package ch.ergon.studentmarks.infrastructure.repository.mapper

import ch.ergon.studentmarks.domain.model.semester.Semester
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.gen.Tables.SEMESTER
import org.jooq.Record

object SemesterRecordMapper {
    fun Record.toSemester(): Semester =
        Semester(
            id = SemesterId(this[SEMESTER.ID]),
            semesterName = this[SEMESTER.NAME],
            startDate = this[SEMESTER.START_DATE],
            endDate = this[SEMESTER.END_DATE],
        )
}
