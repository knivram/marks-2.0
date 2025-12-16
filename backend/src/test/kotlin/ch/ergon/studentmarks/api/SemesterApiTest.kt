package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.semester.PaginatedListDto
import ch.ergon.studentmarks.db.SemesterRepository
import ch.ergon.studentmarks.domain.semester.Semester
import ch.ergon.studentmarks.test.objects.TestSemester
import ch.ergon.studentmarks.test.objects.TestSemester.Companion.SpecialTestSemesters
import ch.ergon.studentmarks.test.objects.TestUser
import ch.ergon.studentmarks.utils.TestUuidUtilsImpl
import ch.ergon.studentmarks.utils.toAddSemesterRequest
import io.restassured.RestAssured.given
import io.restassured.common.mapper.TypeRef
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SemesterApiTest : IntegrationTest() {
    @Autowired
    private lateinit var testUuidUtilsImpl: TestUuidUtilsImpl

    @Autowired
    private lateinit var semesterRepository: SemesterRepository

    @Test
    fun getAllSemester() {
        val response =
            given()
                .withApprentice()
                .get("/api/semesters")
                .then()
                .statusCode(200)
                .extract()
                .response()
                .body()
                .`as`(object : TypeRef<PaginatedListDto<Semester>>() {})

        assertThat(response.list, containsInAnyOrder(*(TestSemester.semesters.toTypedArray())))
        assertThat(response.totalEntries, `is`(TestSemester.semesters.size))
    }

    @Test
    fun get_semesters_with_limit() {
        val response =
            given()
                .withApprentice()
                .get("/api/semesters?limit=2")
                .then()
                .statusCode(200)
                .extract()
                .response()
                .body()
                .`as`(object : TypeRef<PaginatedListDto<Semester>>() {})

        println(response)

        val expectedResponse =
            PaginatedListDto(
                totalEntries = 4,
                offset = 0,
                list = TestSemester.semesters.subList(0, 2),
            )

        assertThat(response, `is`(expectedResponse))
    }

    @Test
    fun get_semesters_with_limit_and_offset() {
        val response =
            given()
                .withApprentice()
                .get("/api/semesters?offset=2&limit=2")
                .then()
                .statusCode(200)
                .extract()
                .response()
                .body()
                .`as`(object : TypeRef<PaginatedListDto<Semester>>() {})

        val expectation =
            PaginatedListDto(
                totalEntries = 4,
                offset = 2,
                list = TestSemester.semesters.subList(2, 4),
            )

        assertThat(response, `is`(expectation))
    }

    @Test
    fun add_semester() {
        testUuidUtilsImpl.setUUID(SpecialTestSemesters.AddSemester.id.value)

        val semester =
            given()
                .withAdmin()
                .accept("application/json")
                .contentType("application/json")
                .body(SpecialTestSemesters.AddSemester.toAddSemesterRequest())
                .post("/api/semesters")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", Semester::class.java)

        assertThat(semester, `is`(SpecialTestSemesters.AddSemester))

        val semesters = semesterRepository.getAllSemester()

        assertThat(semesters, `is`(SpecialTestSemesters.semestersAfterAdding))
    }

    @Test
    fun edit_semester() {
        val semester =
            given()
                .withAdmin()
                .accept("application/json")
                .contentType("application/json")
                .body(SpecialTestSemesters.EditSemester.toAddSemesterRequest())
                .put("/api/semesters/${SpecialTestSemesters.EditSemester.id.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", Semester::class.java)

        assertThat(semester, `is`(SpecialTestSemesters.EditSemester))

        val semesters = semesterRepository.getAllSemester()

        assertThat(semesters, `is`(SpecialTestSemesters.semestersAfterEditing))
    }

    @Test
    fun getRelevantSemesterLernender() {
        val response =
            given()
                .withApprentice()
                .get("/api/semesters/relevant?userId=${TestUser.Lernender.userId.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Semester::class.java)

        println(response)

        assertThat(response, contains(*(TestSemester.semesters.toTypedArray())))
    }
}
