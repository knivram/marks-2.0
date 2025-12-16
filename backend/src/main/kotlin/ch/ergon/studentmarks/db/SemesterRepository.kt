package ch.ergon.studentmarks.db

import ch.ergon.studentmarks.domain.PaginationParams
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.semester.Semester
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.gen.Tables.MARK
import ch.ergon.studentmarks.gen.Tables.SEMESTER
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SemesterRepository(
    private val dsl: DSLContext,
) {
    fun getAllSemester(paginationParams: PaginationParams = PaginationParams(0, 20)): List<Semester> {
        val record =
            dsl
                .selectFrom(SEMESTER)
                .orderBy(SEMESTER.START_DATE.desc())
                .limit(paginationParams.limit)
                .offset(paginationParams.offset)
                .fetch()

        return record.map {
            Semester(SemesterId(it.id), it.name, it.startDate, it.endDate)
        }
    }

    fun getAmountOfSemesters(): Number = dsl.fetchCount(SEMESTER)

    fun addSemester(semester: Semester): Semester =
        dsl
            .insertInto(SEMESTER)
            .set(SEMESTER.ID, semester.id.value)
            .set(SEMESTER.NAME, semester.semesterName)
            .set(SEMESTER.START_DATE, semester.startDate)
            .set(SEMESTER.END_DATE, semester.endDate)
            .returning()
            .fetchOne()!!
            .let { Semester(SemesterId(it.id), it.name, it.startDate, it.endDate) }

    fun getAllRelevantSemester(
        userId: UserId,
        dateNow: LocalDate,
    ): List<Semester> {
        val record =
            dsl
                .selectFrom(SEMESTER)
                .where(SEMESTER.START_DATE.lessOrEqual(dateNow))
                .and(SEMESTER.END_DATE.greaterOrEqual(dateNow))
                .or(
                    SEMESTER.ID.`in`(
                        dsl
                            .select(MARK.SEMESTER_ID)
                            .from(MARK)
                            .where(MARK.USER_ID.eq(userId.value))
                            .groupBy(MARK.SEMESTER_ID),
                    ),
                ).orderBy(SEMESTER.END_DATE.desc())
                .fetch()

        return record.map {
            Semester(SemesterId(it.id), it.name, it.startDate, it.endDate)
        }
    }

    fun editSemester(semester: Semester): Semester =
        dsl
            .update(SEMESTER)
            .set(SEMESTER.NAME, semester.semesterName)
            .set(SEMESTER.START_DATE, semester.startDate)
            .set(SEMESTER.END_DATE, semester.endDate)
            .where(SEMESTER.ID.eq(semester.id.value))
            .returning()
            .fetchOne()!!
            .let { Semester(SemesterId(it.id), it.name, it.startDate, it.endDate) }
}
