package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.generated.dto.UserDto
import ch.ergon.studentmarks.test.objects.TestUser
import io.restassured.RestAssured.given
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class AuthenticationApiTest : IntegrationTest() {
    @Test
    fun readFailsWithoutHeader() {
        given()
            .get("/api/user/info")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
    }

    @Test
    fun canReadWithHeader() {
        val response =
            given()
                .withSupervisor()
                .get("/api/user/info")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", UserDto::class.java)

        assertThat(response.userId, `is`(TestUser.Berufsbildner.userId.value))
    }
}
