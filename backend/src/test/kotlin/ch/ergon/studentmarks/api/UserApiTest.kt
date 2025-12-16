package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.user.ApprenticeConnectRequest
import ch.ergon.studentmarks.api.user.ApprenticeSupervisorPairDto
import ch.ergon.studentmarks.api.user.SimpleUserDto
import ch.ergon.studentmarks.api.user.UserDto
import ch.ergon.studentmarks.api.user.UserRoleDto
import ch.ergon.studentmarks.db.UserRepository
import ch.ergon.studentmarks.domain.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.user.Email
import ch.ergon.studentmarks.domain.user.User
import ch.ergon.studentmarks.domain.user.UserName
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
                .get("/api/user")
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
                .get("/api/user/apprentice")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", SimpleUserDto::class.java)

        val allApprentices = TestUser.Apprentices.map { it.toSimpleUserDto() }

        assertThat(response, containsInAnyOrder(*(allApprentices.toTypedArray())))
    }

    @Test
    fun search_for_apprentice_with_query_Marvin_returns_only_one_mail_api() {
        val response =
            given()
                .withAdmin()
                .get("/api/user/apprentice?searchQuery=Marvin")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", SimpleUserDto::class.java)

        assertThat(response, containsInAnyOrder(Lernender.toSimpleUserDto()))
    }

    @Test
    fun search_for_supervisor_with_empty_query_returns_list_of_all_supervisor_mail_api() {
        val response =
            given()
                .withAdmin()
                .get("/api/user/supervisor")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", SimpleUserDto::class.java)

        val allSupervisors = TestUser.Supervisors.map { it.toSimpleUserDto() }

        assertThat(response, containsInAnyOrder(*(allSupervisors.toTypedArray())))
    }

    @Test
    fun search_for_supervisor_with_query_Boksberger_returns_only_one_mail_api() {
        val response =
            given()
                .withAdmin()
                .get("/api/user/supervisor?searchQuery=Boksberger")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", SimpleUserDto::class.java)

        assertThat(response, containsInAnyOrder(Berufsbildner.toSimpleUserDto()))
    }

    @Test
    fun add_user_with_existing_emails_fails() {
        given()
            .withAdmin()
            .accept("application/json")
            .contentType("application/json")
            .body(SimpleUserDto(Lernender.email.value, Lernender.userName.value, UserRoleDto.APPRENTICE))
            .post("/api/user")
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
            .body(ApprenticeConnectRequest(Email("non.existing@mail.com"), Admin.email))
            .post("/api/user/connect")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun connect_apprentice_to_supervisor() {
        val connection =
            given()
                .withAdmin()
                .contentType("application/json")
                .body(ApprenticeConnectRequest(Lernender.email, Admin.email))
                .post("/api/user/connect")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject(".", ApprenticeSupervisorPairDto::class.java)

        val result =
            given()
                .withAdmin()
                .accept("application/json")
                .get("/api/user/connections")
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
        val updatedConnection = TestConnections.connection1.copy(emailNotification = false).toDto()
        val connections =
            given()
                .withSupervisor()
                .contentType("application/json")
                .body(listOf(updatedConnection))
                .put("/api/user/connections")
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
                .get("/api/user/connections")
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
                .delete("/api/user/connections/${connection1.id.value}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject(".", ApprenticeSupervisorPairDto::class.java)

        val result = userRepository.getApprenticeSupervisorPairs()

        val listWithoutDeletedConnections =
            TestConnections.connections.filter { pair ->
                pair.id.value !=
                    TestConnections.connection1.id.value
            }

        val supervisor =
            User(
                TestConnections.connection1.supervisor.userId,
                TestConnections.connection1.supervisor.userName,
                TestConnections.connection1.supervisor.email,
                TestConnections.connection1.supervisor.role,
            )

        assertThat(
            deletedConnection,
            `is`(
                ApprenticeSupervisorPair(
                    connection1.id,
                    connection1.apprentice,
                    supervisor,
                    connection1.emailNotification,
                ).toDto(),
            ),
        )
        assertThat(result, containsInAnyOrder(*(listWithoutDeletedConnections.toTypedArray())))
    }

    @Test
    fun get_all_deleted_users() {
        val result =
            given()
                .withAdmin()
                .accept("application/json")
                .get("/api/user/deletedUsers")
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
                .delete("/api/user/${Lernender.userId.value}")
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
                .delete("/api/user/deletedUsers/${DeletedUser.userId.value}")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject(".", UserDto::class.java)

        assertThat(deletedUser, `is`(DeletedUser.toDto()))

        assertThat(userRepository.getDeletedUsers(), empty())
    }

    @Test
    fun restore_user_returns_the_restored_user() {
        val result =
            given()
                .withAdmin()
                .accept("application/json")
                .post("/api/user/${DeletedUser.userId.value}/restore")
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
            .delete("/api/user/${Admin.userId.value}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun edit_user_boxi() {
        val editedUserBoxi =
            User(Berufsbildner.userId, userName = UserName("Boxi_new"), Berufsbildner.email, Berufsbildner.role).toDto()

        val editedUser =
            given()
                .withAdmin()
                .accept("application/json")
                .contentType("application/json")
                .body(editedUserBoxi)
                .put("/api/user/${Berufsbildner.userId.value}/edit")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject(".", UserDto::class.java)

        assertThat(editedUser, `is`(editedUser))
    }
}
