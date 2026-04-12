package ch.ergon.studentmarks.permission

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.withApprentice
import ch.ergon.studentmarks.api.withSupervisor
import ch.ergon.studentmarks.api.generated.dto.MarkDto
import ch.ergon.studentmarks.api.toDto
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.LernenderMarks
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.SpecialMarks
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Jan
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import io.restassured.RestAssured
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class PermissionTest : IntegrationTest() {
    @Test
    fun supervisorCanReadOfApprentice() {
        val response =
            RestAssured
                .given()
                .withSupervisor()
                .get("/api/marks/future-exams?userId=${Lernender.userId.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", MarkDto::class.java)

        assertThat(response, containsInAnyOrder(*(LernenderMarks.FutureMark.marks.map { it.toDto() }.toTypedArray())))
    }

    @Test
    fun failsOnApprenticeToReadOtherApprentice() {
        RestAssured
            .given()
            .withApprentice()
            .get("/api/marks/future-exams?userId=${Jan.userId.value}")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun supervisorCanNotEditMark() {
        RestAssured
            .given()
            .withSupervisor()
            .contentType("application/json")
            .body(SpecialMarks.EditedMathMark.toDto())
            .post("/api/marks/${SpecialMarks.EditedMathMark.id.value}/edit")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun supervisorCanNotDeleteMark() {
        RestAssured
            .given()
            .withSupervisor()
            .delete("api/marks/${SpecialMarks.EditedMathMark.id.value}/delete")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun unknownUserCannotExec() {
        RestAssured
            .given()
            .header("X-Forwarded-Mail", "unknown.user@gmail.com")
            .get("api/user/info")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
    }
}
