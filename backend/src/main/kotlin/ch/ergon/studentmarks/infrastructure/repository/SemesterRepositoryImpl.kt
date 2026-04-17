package ch.ergon.studentmarks.infrastructure.repository

import ch.ergon.studentmarks.domain.model.semester.Semester
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.shared.PaginatedList
import ch.ergon.studentmarks.domain.model.shared.PaginationParams
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.SemesterRepository
import ch.ergon.studentmarks.gen.Tables.MARK
import ch.ergon.studentmarks.gen.Tables.SEMESTER
import ch.ergon.studentmarks.infrastructure.repository.mapper.SemesterRecordMapper.toSemester
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class SemesterRepositoryImpl(
    private val dsl: DSLContext,
) : SemesterRepository {
    override fun getAll(paginationParams: PaginationParams): PaginatedList<Semester> {
        val semesters =
            dsl
                .selectFrom(SEMESTER)
                .orderBy(SEMESTER.START_DATE.desc())
                .limit(paginationParams.limit)
                .offset(paginationParams.offset)
                .fetch()
                .map { it.toSemester() }

        val total = count()
        return PaginatedList(total, semesters)
    }

    override fun getById(semesterId: SemesterId): Semester =
        dsl
            .selectFrom(SEMESTER)
            .where(SEMESTER.ID.eq(semesterId.value))
            .fetchOne()
            ?.toSemester()
            ?: throw NoSuchElementException("Semester with id ${semesterId.value} not found")

    override fun save(semester: Semester): Semester =
        dsl
            .insertInto(SEMESTER)
            .set(SEMESTER.ID, semester.id.value)
            .set(SEMESTER.NAME, semester.semesterName)
            .set(SEMESTER.START_DATE, semester.startDate)
            .set(SEMESTER.END_DATE, semester.endDate)
            .returning()
            .fetchOne()!!
            .toSemester()

    override fun update(semester: Semester): Semester =
        dsl
            .update(SEMESTER)
            .set(SEMESTER.NAME, semester.semesterName)
            .set(SEMESTER.START_DATE, semester.startDate)
            .set(SEMESTER.END_DATE, semester.endDate)
            .where(SEMESTER.ID.eq(semester.id.value))
            .returning()
            .fetchOne()!!
            .toSemester()

    override fun findRelevantForUser(
        userId: UserId,
        asOf: LocalDate,
    ): List<Semester> =
        dsl
            .selectFrom(SEMESTER)
            .where(SEMESTER.START_DATE.lessOrEqual(asOf))
            .and(SEMESTER.END_DATE.greaterOrEqual(asOf))
            .or(
                SEMESTER.ID.`in`(
                    dsl
                        .select(MARK.SEMESTER_ID)
                        .from(MARK)
                        .where(MARK.USER_ID.eq(userId.value))
                        .andNot(MARK.DELETED)
                        .groupBy(MARK.SEMESTER_ID),
                ),
            ).orderBy(SEMESTER.END_DATE.desc())
            .fetch()
            .map { it.toSemester() }

   private fun count(): Int = dsl.fetchCount(SEMESTER)
}
