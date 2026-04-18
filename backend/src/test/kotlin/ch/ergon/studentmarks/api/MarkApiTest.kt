package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.generated.dto.AgendaDto
import ch.ergon.studentmarks.api.generated.dto.MarkDto
import ch.ergon.studentmarks.api.generated.dto.MarkValueDto
import ch.ergon.studentmarks.api.generated.dto.MarkWithSubjectDto
import ch.ergon.studentmarks.api.generated.dto.SemesterOverviewDto
import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.LernenderMarks
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.SpecialMarks
import ch.ergon.studentmarks.test.objects.TestSemester
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import ch.ergon.studentmarks.utils.TestTimeUtilsImpl
import ch.ergon.studentmarks.utils.TestUuidUtilsImpl
import ch.ergon.studentmarks.utils.toDto
import ch.ergon.studentmarks.utils.toMarkRequestDto
import ch.ergon.studentmarks.utils.toMarkWithSubjectDto
import io.restassured.RestAssured.given
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class MarkApiTest : IntegrationTest() {
    @Autowired
    private lateinit var testUuidUtilsImpl: TestUuidUtilsImpl

    @Autowired
    private lateinit var testTimeUtilsImpl: TestTimeUtilsImpl

    @Autowired
    private lateinit var markRepository: MarkRepository

    @Test
    fun getSemesterOverview() {
        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .get(
                    "/api/semesters/${TestSemester.Autumn22.id.value}/overview" +
                        "?userId=${Lernender.userId.value}",
                ).then()
                .statusCode(200)
                .extract()
                .`as`(SemesterOverviewDto::class.java)

        // Autumn22 has math1, math2 (Math) and french2 (French) — all general subjects
        val allMarks =
            response.generalSubjects.flatMap { it.marks } +
                response.vocationalSubjects.flatMap { it.marks }

        assertThat(
            allMarks,
            containsInAnyOrder(
                *(LernenderMarks.autumn22.map { it.toDto() }.toTypedArray()),
            ),
        )
        assertThat(response.vocationalSubjects, hasSize(0))
    }

    @Test
    fun getAgenda() {
        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .get("/api/agenda?userId=${Lernender.userId.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", AgendaDto::class.java)

        // Default time is 2023-05-31.
        // FutureMark.marks (2023-12-02 and 2024-01-24) are upcoming exams
        assertThat(
            response.upcomingExams,
            containsInAnyOrder(
                *(LernenderMarks.FutureMark.marks.map { it.toMarkWithSubjectDto() }.toTypedArray()),
            ),
        )

        // PastMarks (2023-04-04, null values) are past exams without grade
        assertThat(
            response.pastExamsWithoutGrade,
            containsInAnyOrder(
                *(LernenderMarks.PastMarks.marks.map { it.toMarkWithSubjectDto() }.toTypedArray()),
            ),
        )
    }

    @Test
    fun addNewMark() {
        testUuidUtilsImpl.setUUID(SpecialMarks.MarkForAddTest.id.value)

        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .body(SpecialMarks.MarkForAddTest.toMarkRequestDto())
                .post("/api/marks")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("mark", MarkDto::class.java)

        assertThat(response, `is`(SpecialMarks.MarkForAddTest.toDto()))
    }

    @Test
    fun editMark() {
        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .body(SpecialMarks.EditedMathMark.toMarkRequestDto())
                .put("/api/marks/${SpecialMarks.EditedMathMark.id.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("mark", MarkDto::class.java)

        assertThat(response, `is`(SpecialMarks.EditedMathMark.toDto()))
    }

    @Test
    fun deleteMark() {
        given()
            .withApprentice()
            .accept("application/json")
            .contentType("application/json")
            .delete("/api/marks/${LernenderMarks.math1.id.value}")
            .then()
            .statusCode(200)

        assertThat(markRepository.findByUser(Lernender.userId), not(hasItem(LernenderMarks.math1)))
    }

    @Test
    fun restoreMark() {
        val response2 =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .post("/api/marks/${SpecialMarks.DeletedMark.id.value}/restore")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("mark", MarkDto::class.java)

        assertThat(response2, `is`(SpecialMarks.DeletedMark.toDto()))

        assertThat(markRepository.findByUser(Lernender.userId), hasItem(SpecialMarks.DeletedMark))
    }

    @Test
    fun deleteAndRestore() {
        given()
            .withApprentice()
            .accept("application/json")
            .contentType("application/json")
            .delete("/api/marks/${LernenderMarks.math1.id.value}")
            .then()
            .statusCode(200)

        assertThat(markRepository.findByUser(Lernender.userId), not(hasItem(LernenderMarks.math1)))
        assertThat(markRepository.findDeleted(Lernender.userId), hasItem(LernenderMarks.math1))

        val response2 =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .post("/api/marks/${LernenderMarks.math1.id.value}/restore")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject("mark", MarkDto::class.java)

        assertThat(response2, `is`(LernenderMarks.math1.toDto()))
        assertThat(markRepository.findByUser(Lernender.userId), hasItem(LernenderMarks.math1))
        assertThat(markRepository.findDeleted(Lernender.userId), not(hasItem(LernenderMarks.math1)))
    }

    @Test
    fun calculateNeededMark() {
        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .get(
                    "/api/marks/calculate" +
                        "?semesterId=${TestSemester.Autumn22.id.value}" +
                        "&userId=${Lernender.userId.value}" +
                        "&subjectId=${TestSubjects.Math.id.value}" +
                        "&goalMark=${4900}" +
                        "&examWeight=${2000}",
                ).then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", MarkValueDto::class.java)

        assertThat(response.value, `is`(4000))
    }

    @Test
    fun calculateNeededMarkIgnoresNullValue() {
        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .get(
                    "/api/marks/calculate" +
                        "?semesterId=${TestSemester.Spring23.id.value}" +
                        "&userId=${Lernender.userId.value}" +
                        "&subjectId=${TestSubjects.English.id.value}" +
                        "&goalMark=${5000}" +
                        "&examWeight=${1000}",
                ).then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", MarkValueDto::class.java)

        assertThat(response.value, `is`(5250))
    }
}
