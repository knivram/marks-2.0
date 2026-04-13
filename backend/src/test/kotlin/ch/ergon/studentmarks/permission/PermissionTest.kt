package ch.ergon.studentmarks.permission

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.withSupervisor
import ch.ergon.studentmarks.infrastructure.security.authentication.SUBJECT_HEADER_NAME
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.SpecialMarks
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import ch.ergon.studentmarks.utils.toDto
import io.restassured.RestAssured
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

@Suppress("ktlint:standard:max-line-length")
private const val JAN_SUBJECT =
    "emailAddress%3Djan.sigrist%40ergon.ch%2CCN%3DJan%20Sigrist%2COU%3DIT%2CO%3DErgon%20Informatik%20AG%2CL%3DZuerich%2CST%3DZuerich%2CC%3DCH"

class PermissionTest : IntegrationTest() {
    @Test
    fun supervisorCanReadAgendaOfApprentice() {
        RestAssured
            .given()
            .withSupervisor()
            .get("/api/agenda?userId=${Lernender.userId.value}")
            .then()
            .statusCode(HttpStatus.OK.value())
    }

    @Test
    fun failsOnApprenticeReadingOtherApprenticeAgenda() {
        RestAssured
            .given()
            .header(SUBJECT_HEADER_NAME, JAN_SUBJECT)
            .get("/api/agenda?userId=${Lernender.userId.value}")
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
