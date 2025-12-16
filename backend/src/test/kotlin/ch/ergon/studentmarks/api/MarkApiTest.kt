package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.db.MarkRepository
import ch.ergon.studentmarks.domain.marks.Mark
import ch.ergon.studentmarks.domain.marks.MarkId
import ch.ergon.studentmarks.domain.marks.MarkValue
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.LernenderMarks
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.SpecialMarks
import ch.ergon.studentmarks.test.objects.TestSemester
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import ch.ergon.studentmarks.utils.TestTimeUtilsImpl
import ch.ergon.studentmarks.utils.TestUuidUtilsImpl
import io.restassured.RestAssured.given
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.core.Is
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class MarkApiTest : IntegrationTest() {
    @Autowired
    private lateinit var testUuidUtilsImpl: TestUuidUtilsImpl

    @Autowired
    private lateinit var testTimeUtilsImpl: TestTimeUtilsImpl

    @Autowired
    private lateinit var markRepository: MarkRepository

    @Test
    fun getAllMarks() {
        val body =
            given()
                .withApprentice()
                .get("/api/marks?semesterId=${TestSemester.Autumn22.id.value}&userId=${Lernender.userId.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Mark::class.java)

        assertThat(body, containsInAnyOrder(*(LernenderMarks.autumn22.toTypedArray())))
    }

    @Test
    fun getMarksFromSubject() {
        val body =
            given()
                .withApprentice()
                .get(
                    "/api/marks" +
                        "?subjectId=${TestSubjects.English.id.value}" +
                        "&semesterId=${TestSemester.Spring23.id.value}" +
                        "&userId=${Lernender.userId.value}",
                ).then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Mark::class.java)

        assertThat(body, containsInAnyOrder(*(LernenderMarks.spring23.toTypedArray())))
    }

    @Test
    fun getFutureExams() {
        val response =
            given()
                .withApprentice()
                .get("/api/marks/future-exams?userId=${Lernender.userId.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Mark::class.java)

        assertThat(response, containsInAnyOrder(*(LernenderMarks.FutureMark.marks.toTypedArray())))
    }

    @Test
    fun getPassedExams() {
        val response =
            given()
                .withApprentice()
                .get("/api/marks/passed-exams?userId=${Lernender.userId.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Mark::class.java)

        assertThat(response, containsInAnyOrder(*(LernenderMarks.PastMarks.marks.toTypedArray())))
    }

    @Test
    fun getDefaultLatestMarks() {
        val response =
            given()
                .withApprentice()
                .get("/api/marks/last?userId=${Lernender.userId.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Mark::class.java)

        assertThat(response, contains(*(LernenderMarks.latest4Marks.toTypedArray())))
    }

    @Test
    fun getLatestMarksLimit5() {
        val response =
            given()
                .withApprentice()
                .get("/api/marks/last?userId=${Lernender.userId.value}&limit=5")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Mark::class.java)

        assertThat(response, contains(*(LernenderMarks.latest5Marks.toTypedArray())))
    }

    @Test
    fun getLatestMarksDefaultWhenOnly3Exist() {
        val testNow = LocalDate.parse("2022-12-19")
        testTimeUtilsImpl.setTime(testNow)
        val response =
            given()
                .withApprentice()
                .get("/api/marks/last?userId=${Lernender.userId.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Mark::class.java)

        assertThat(
            response,
            containsInAnyOrder(*(LernenderMarks.latest5Marks.filter { it.date.isBefore(testNow) }.toTypedArray())),
        )
    }

    @Test
    fun getLatestMarksDefaultMarkOnSameDate() {
        val testNow = LocalDate.parse("2023-05-01")

        testTimeUtilsImpl.setTime(testNow)
        val response =
            given()
                .withApprentice()
                .get("/api/marks/last?userId=${Lernender.userId.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Mark::class.java)

        assertThat(response, contains(*(LernenderMarks.latest4Marks.toTypedArray())))
    }

    @Test
    fun addNewMark() {
        testUuidUtilsImpl.setUUID(SpecialMarks.MarkForAddTest.id.value)

        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .body(SpecialMarks.MarkForAddTest)
                .post("/api/marks?semesterId=${TestSemester.Autumn22.id.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", Mark::class.java)

        assertThat(response, Is.`is`(SpecialMarks.MarkForAddTest))
    }

    @Test
    fun editMark() {
        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .body(SpecialMarks.EditedMathMark)
                .post("/api/marks/${SpecialMarks.EditedMathMark.id.value}/edit")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", Mark::class.java)

        assertThat(response, `is`(SpecialMarks.EditedMathMark))
    }

    @Test
    fun deleteMark() {
        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .delete("api/marks/${LernenderMarks.math1.id.value}/delete")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", MarkId::class.java)

        assertThat(response, `is`(LernenderMarks.math1.id))
        assertThat(markRepository.getMarks(Lernender.userId), not(hasItem(LernenderMarks.math1)))
    }

    @Test
    fun restoreMark() {
        val response2 =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .post("api/marks/${SpecialMarks.DeletedMark.id.value}/restore")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", Mark::class.java)

        assertThat(response2, `is`(SpecialMarks.DeletedMark))

        assertThat(markRepository.getMarks(Lernender.userId), hasItem(SpecialMarks.DeletedMark))
    }

    @Test
    fun deleteAndRestore() {
        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .delete("api/marks/${LernenderMarks.math1.id.value}/delete")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", MarkId::class.java)

        assertThat(response, `is`(LernenderMarks.math1.id))
        assertThat(markRepository.getMarks(Lernender.userId), not(hasItem(LernenderMarks.math1)))
        assertThat(markRepository.getDeletedMarks(Lernender.userId), hasItem(LernenderMarks.math1))

        val response2 =
            given()
                .withApprentice()
                .accept("application/json")
                .contentType("application/json")
                .post("api/marks/${LernenderMarks.math1.id.value}/restore")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", Mark::class.java)

        assertThat(response2, `is`(LernenderMarks.math1))
        assertThat(markRepository.getMarks(Lernender.userId), hasItem(LernenderMarks.math1))
        assertThat(markRepository.getDeletedMarks(Lernender.userId), not(hasItem(LernenderMarks.math1)))
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
                .getObject(".", MarkValue::class.java)

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
                .getObject(".", MarkValue::class.java)

        assertThat(response.value, `is`(5250))
    }
}
