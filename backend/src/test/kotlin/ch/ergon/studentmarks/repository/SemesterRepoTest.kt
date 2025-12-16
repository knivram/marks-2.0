package ch.ergon.studentmarks.repository

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.db.SemesterRepository
import ch.ergon.studentmarks.domain.PaginationParams
import ch.ergon.studentmarks.test.objects.TestSemester
import ch.ergon.studentmarks.test.objects.TestSemester.Companion.SpecialTestSemesters
import ch.ergon.studentmarks.test.objects.TestUser
import ch.ergon.studentmarks.utils.TestTimeUtilsImpl
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SemesterRepoTest : IntegrationTest() {
    @Autowired
    private lateinit var semesterRepository: SemesterRepository

    @Autowired
    private lateinit var testTimeUtilsImpl: TestTimeUtilsImpl

    @Test
    fun getAllSemester() {
        val semesters = semesterRepository.getAllSemester()

        assertThat(semesters, `is`(TestSemester.semesters))
    }

    @Test
    fun get_semesters_with_limit() {
        val semesters = semesterRepository.getAllSemester(PaginationParams(0, 2))

        assertThat(semesters, `is`(TestSemester.semesters.subList(0, 2)))
    }

    @Test
    fun get_semesters_with_limit_and_offset() {
        val semesters = semesterRepository.getAllSemester(PaginationParams(2, 2))

        assertThat(semesters, `is`(TestSemester.semesters.subList(2, 4)))
    }

    @Test
    fun add_semester() {
        val semester = semesterRepository.addSemester(SpecialTestSemesters.AddSemester)

        assertThat(semester, `is`(SpecialTestSemesters.AddSemester))

        val semesters = semesterRepository.getAllSemester()

        assertThat(semesters, `is`(SpecialTestSemesters.semestersAfterAdding))
    }

    @Test
    fun edit_semester() {
        val semester = semesterRepository.editSemester(SpecialTestSemesters.EditSemester)

        assertThat(semester, `is`(SpecialTestSemesters.EditSemester))

        val semesters = semesterRepository.getAllSemester()

        assertThat(semesters, `is`(SpecialTestSemesters.semestersAfterEditing))
    }

    @Test
    fun getAllRelevantSemester() {
        val semesters = semesterRepository.getAllRelevantSemester(TestUser.Lernender.userId, testTimeUtilsImpl.now())

        assertThat(semesters, contains(*(TestSemester.semesters.toTypedArray())))
    }
}
