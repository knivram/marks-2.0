package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.generated.dto.ApprenticeConnectRequestDto
import ch.ergon.studentmarks.api.generated.dto.ApprenticeSupervisorPairDto
import ch.ergon.studentmarks.api.generated.dto.UserCreateRequestDto
import ch.ergon.studentmarks.api.generated.dto.UserDto
import ch.ergon.studentmarks.api.generated.dto.UserRoleDto
import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserName
import ch.ergon.studentmarks.domain.repository.UserRepository
import ch.ergon.studentmarks.test.objects.TestConnections
import ch.ergon.studentmarks.test.objects.TestUser
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Admin
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Berufsbildner
import ch.ergon.studentmarks.test.objects.TestUser.Companion.DeletedUser
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import ch.ergon.studentmarks.test.objects.TestUser.Companion.UserDtos
import io.restassured.RestAssured.given
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class UserApiTest : IntegrationTest() {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun getCurrentUser() {
        val response =
            given()
                .withSupervisor()
                .get("/api/user/info")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", UserDto::class.java)

        assertThat(response, `is`(Berufsbildner.toDto()))
    }

    @Test
    fun getAdminWithAllApprentice() {
        val response =
            given()
                .withAdmin()
                .get("/api/user/info")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", UserDto::class.java)

        assertThat(response, `is`(Admin.toDto()))
    }

    @Test
    fun get_all_users() {
        val result =
            given()
                .withAdmin()
                .accept("application/json")
                .get("/api/admin/users")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", UserDto::class.java)

        assertThat(result, containsInAnyOrder(*(UserDtos.toTypedArray())))
    }

    @Test
    fun search_for_apprentice_with_empty_query_returns_list_of_all_apprentice_mail_api() {
        val response =
            given()
                .withAdmin()
                .get("/api/admin/users/apprentice")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", UserDto::class.java)

        val allApprentices = TestUser.Apprentices.map { it.toDto() }

        assertThat(response, containsInAnyOrder(*(allApprentices.toTypedArray())))
    }

    @Test
    fun search_for_apprentice_with_query_Marvin_returns_only_one_mail_api() {
        val response =
            given()
                .withAdmin()
                .get("/api/admin/users/apprentice?searchQuery=Marvin")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", UserDto::class.java)

        assertThat(response, containsInAnyOrder(Lernender.toDto()))
    }

    @Test
    fun search_for_supervisor_with_empty_query_returns_list_of_all_supervisor_mail_api() {
        val response =
            given()
                .withAdmin()
                .get("/api/admin/users/supervisor")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", UserDto::class.java)

        val allSupervisors = TestUser.Supervisors.map { it.toDto() }

        assertThat(response, containsInAnyOrder(*(allSupervisors.toTypedArray())))
    }

    @Test
    fun search_for_supervisor_with_query_Boksberger_returns_only_one_mail_api() {
        val response =
            given()
                .withAdmin()
                .get("/api/admin/users/supervisor?searchQuery=Boksberger")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", UserDto::class.java)

        assertThat(response, containsInAnyOrder(Berufsbildner.toDto()))
    }

    @Test
    fun add_user_with_existing_emails_fails() {
        given()
            .withAdmin()
            .accept("application/json")
            .contentType("application/json")
            .body(UserCreateRequestDto(Lernender.email.value, Lernender.userName.value, UserRoleDto.APPRENTICE))
            .post("/api/admin/users")
            .then()
            .statusCode(HttpStatus.CONFLICT.value())
    }

    @Test
    fun supervisor_can_get_his_apprentice() {
        val response =
            given()
                .withSupervisor()
                .get("/api/user/${Berufsbildner.userId.value}/apprentice")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", UserDto::class.java)

        assertThat(response, contains(Lernender.toDto()))
    }

    @Test
    fun user_should_not_be_able_to_get_apprentice_of_different_user() {
        given()
            .withSupervisor()
            .get("/api/user/${Admin.userId.value}/apprentice")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun connect_apprentice_to_supervisor_with_non_existing_email_fails() {
        given()
            .withAdmin()
            .contentType("application/json")
            .body(ApprenticeConnectRequestDto(apprenticeEmail = "non.existing@mail.com", supervisorEmail = Admin.email.value))
            .post("/api/admin/connections")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun connect_apprentice_to_supervisor() {
        val connection =
            given()
                .withAdmin()
                .contentType("application/json")
                .body(ApprenticeConnectRequestDto(apprenticeEmail = Lernender.email.value, supervisorEmail = Admin.email.value))
                .post("/api/admin/connections")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject(".", ApprenticeSupervisorPairDto::class.java)

        val result =
            given()
                .withAdmin()
                .accept("application/json")
                .get("/api/admin/connections")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", ApprenticeSupervisorPairDto::class.java)

        val testApprentice = Lernender.toDto()
        val testSupervisor = Admin.toDto()

        assertThat(
            result,
            containsInAnyOrder(
                *(TestConnections.connectionDtos.toTypedArray()),
                ApprenticeSupervisorPairDto(
                    connection.id,
                    testApprentice,
                    testSupervisor,
                    connection.emailNotification,
                ),
            ),
        )
    }

    @Test
    fun update_apprentice_supervisor_connection() {
        val updatedConnection = TestConnections.connectionDtos.first().copy(emailNotification = false)
        val connections =
            given()
                .withAdmin()
                .contentType("application/json")
                .body(listOf(updatedConnection))
                .put("/api/admin/connections")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", ApprenticeSupervisorPairDto::class.java)

        assertThat(connections, containsInAnyOrder(updatedConnection))
    }

    @Test
    fun get_all_apprentice_supervisor_pairs() {
        val result =
            given()
                .withAdmin()
                .accept("application/json")
                .get("/api/admin/connections")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", ApprenticeSupervisorPairDto::class.java)

        assertThat(result, containsInAnyOrder(*(TestConnections.connectionDtos.toTypedArray())))
    }

    @Test
    fun delete_apprenticeSupervisor_connection() {
        val connection1 = TestConnections.connection1
        val deletedConnection =
            given()
                .withAdmin()
                .accept("application/json")
                .delete("/api/admin/connections/${connection1.id.value}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject(".", ApprenticeSupervisorPairDto::class.java)

        // Verify the connection was deleted by checking remaining connections
        val result =
            given()
                .withAdmin()
                .accept("application/json")
                .get("/api/admin/connections")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", ApprenticeSupervisorPairDto::class.java)

        val listWithoutDeletedConnections =
            TestConnections.connectionDtos.filter { it.id != connection1.id.value }

        assertThat(result, containsInAnyOrder(*(listWithoutDeletedConnections.toTypedArray())))
    }

    @Test
    fun get_all_deleted_users() {
        val result =
            given()
                .withAdmin()
                .accept("application/json")
                .get("/api/admin/users/deleted")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", UserDto::class.java)

        assertThat(result, containsInAnyOrder(DeletedUser.toDto()))
    }

    @Test
    fun delete_user_returns_the_deleted_user() {
        val result =
            given()
                .withAdmin()
                .accept("application/json")
                .delete("/api/admin/users/${Lernender.userId.value}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject(".", UserDto::class.java)

        assertThat(result, `is`(Lernender.toDto()))
    }

    @Test
    fun delete_deletedUser() {
        val deletedUser =
            given()
                .withAdmin()
                .accept("application/json")
                .delete("/api/admin/users/deleted/${DeletedUser.userId.value}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject(".", UserDto::class.java)

        assertThat(deletedUser, `is`(DeletedUser.toDto()))

        assertThat(userRepository.findDeleted(), empty())
    }

    @Test
    fun restore_user_returns_the_restored_user() {
        val result =
            given()
                .withAdmin()
                .accept("application/json")
                .post("/api/admin/users/${DeletedUser.userId.value}/restore")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject(".", UserDto::class.java)

        assertThat(result, `is`(DeletedUser.toDto()))
    }

    @Test
    fun delete_own_account_will_fail() {
        given()
            .withAdmin()
            .accept("application/json")
            .delete("/api/admin/users/${Admin.userId.value}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun edit_user_boxi() {
        val editedUserBoxi =
            User(Berufsbildner.userId, userName = UserName("boxi_new"), Berufsbildner.email, Berufsbildner.role).toDto()

        val editedUser =
            given()
                .withAdmin()
                .accept("application/json")
                .contentType("application/json")
                .body(editedUserBoxi)
                .put("/api/admin/users/${Berufsbildner.userId.value}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject(".", UserDto::class.java)

        assertThat(editedUser, `is`(editedUserBoxi))
    }
}
