package ch.ergon.studentmarks.permission

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.withApprentice
import ch.ergon.studentmarks.api.withSupervisor
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.SpecialMarks
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Jan
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import ch.ergon.studentmarks.utils.toDto
import io.restassured.RestAssured
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class PermissionTest : IntegrationTest() {
    // TODO: Old endpoint /api/marks/future-exams no longer exists. Replaced by /api/agenda.
    // Rewrite this test to use the new /api/agenda endpoint.
    // @Test
    // fun supervisorCanReadOfApprentice() { ... }

    // TODO: Old endpoint /api/marks/future-exams no longer exists.
    // @Test
    // fun failsOnApprenticeToReadOtherApprentice() { ... }

    @Test
    fun supervisorCanNotEditMark() {
        RestAssured
            .given()
            .withSupervisor()
            .contentType("application/json")
            .body(SpecialMarks.EditedMathMark.toDto())
            .put("/api/marks/${SpecialMarks.EditedMathMark.id.value}")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun supervisorCanNotDeleteMark() {
        RestAssured
            .given()
            .withSupervisor()
            .delete("/api/marks/${SpecialMarks.EditedMathMark.id.value}")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun unknownUserCannotExec() {
        RestAssured
            .given()
            .header("X-Forwarded-Mail", "unknown.user@gmail.com")
            .get("/api/user/info")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
    }
}
