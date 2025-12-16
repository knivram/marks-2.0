package ch.ergon.studentmarks.repository

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.toSimpleUser
import ch.ergon.studentmarks.db.UserRepository
import ch.ergon.studentmarks.domain.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.user.Email
import ch.ergon.studentmarks.domain.user.SimpleUser
import ch.ergon.studentmarks.domain.user.User
import ch.ergon.studentmarks.domain.user.UserName
import ch.ergon.studentmarks.domain.user.UserRole
import ch.ergon.studentmarks.test.objects.TestConnections
import ch.ergon.studentmarks.test.objects.TestUser
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Admin
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Berufsbildner
import ch.ergon.studentmarks.test.objects.TestUser.Companion.DeletedUser
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Kevin
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Noah
import ch.ergon.studentmarks.test.objects.TestUser.Companion.UnusedUser
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsInRelativeOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UserRepoTest : IntegrationTest() {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun getUser() {
        val result = userRepository.getUser(Lernender.email)

        assertThat(result, `is`(Lernender))
    }

    @Test
    fun supervisorListOfApprenticeContainsAnApprentices() {
        val result = userRepository.getApprentice(Berufsbildner)

        assertThat(result, hasSize(1))
        assertThat(result, containsInAnyOrder(Lernender))
    }

    @Test
    fun list_of_supervisor_contains_the_supervisor_of_the_selected_apprentice() {
        val result = userRepository.getNotificationEmailListForApprentice(Lernender.userId)

        assertThat(result, hasSize(1))
        assertThat(result, containsInAnyOrder(Berufsbildner.toSimpleUser()))
    }

    @Test
    fun list_of_supervisor_contains_all_supervisors_of_an_apprentice_only_once() {
        val result = userRepository.getNotificationEmailListForApprentice(Kevin.userId)

        assertThat(result, hasSize(1))
        assertThat(result, containsInAnyOrder(Noah.toSimpleUser()))
    }

    @Test
    fun list_of_apprentices_is_empty_for_apprentices() {
        val result = userRepository.getApprentice(Lernender)

        assertThat(result, empty())
    }

    @Test
    fun search_for_apprentice_without_query_returns_list_of_all_apprentice_mail() {
        val result = userRepository.getApprentice()

        assertThat(result, containsInAnyOrder(*(TestUser.Apprentices.map { it.toSimpleUser() }.toTypedArray())))
    }

    @Test
    fun search_for_apprentice_with_query_Marvin_returns_only_one_mail() {
        val result = userRepository.getApprentice("Marvin")

        assertThat(result, containsInAnyOrder(Lernender.toSimpleUser()))
    }

    @Test
    fun search_for_supervisor_without_query_returns_list_of_all_supervisor_mail() {
        val result = userRepository.getSupervisors()

        assertThat(result, containsInAnyOrder(*(TestUser.Supervisors.map { it.toSimpleUser() }.toTypedArray())))
    }

    @Test
    fun search_for_supervisor_with_query_Boksberger_returns_only_one_mail() {
        val result = userRepository.getSupervisors("Boksberger")

        assertThat(
            result,
            containsInAnyOrder(Berufsbildner.toSimpleUser()),
        )
    }

    @Test
    fun get_all_users() {
        val result = userRepository.getUsers()
        assertThat(result, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray())))
    }

    @Test
    fun create_new_user() {
        val newUser = SimpleUser(Email("marvin@ergon.ch"), UserName("kmarvin"), UserRole.APPRENTICE)
        val user = userRepository.createUser(newUser)
        val result = userRepository.getUsers()

        assertThat(result, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray()), user))
    }

    @Test
    fun get_deleted_user_returns_all_deleted_users() {
        val deletedUsers = userRepository.getDeletedUsers()
        assertThat(deletedUsers, containsInAnyOrder(DeletedUser))
    }

    @Test
    fun delete_user() {
        val usersBefore = userRepository.getUsers()
        assertThat(usersBefore, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray())))

        userRepository.deleteUser(UnusedUser.userId)

        val users = userRepository.getUsers()
        val deletedUsers = userRepository.getDeletedUsers()
        val testUsersWithoutDeletedOne = TestUser.AllUsers.filter { it.userId != UnusedUser.userId }

        assertThat(users, containsInAnyOrder(*(testUsersWithoutDeletedOne.toTypedArray())))
        assertThat(deletedUsers, containsInAnyOrder(DeletedUser, UnusedUser))
    }

    @Test
    fun after_deletion_user_is_in_deleted_Table() {
        userRepository.deleteUser(UnusedUser.userId)

        val deletedUsers = userRepository.getDeletedUsers()

        assertThat(deletedUsers, containsInAnyOrder(DeletedUser, UnusedUser))
    }

    @Test
    fun restore_deleted_user() {
        userRepository.restoreUser(DeletedUser.userId)

        val deletedUsers = userRepository.getDeletedUsers()
        assertThat(deletedUsers, empty())

        val users = userRepository.getUsers()
        assertThat(users, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray()), DeletedUser))
    }

    @Test
    fun delete_and_restore_after() {
        var users = userRepository.getUsers()
        assertThat(users, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray())))

        userRepository.deleteUser(UnusedUser.userId)

        users = userRepository.getUsers()

        val testUsersWithoutDeletedOne = TestUser.AllUsers.filter { it.userId != UnusedUser.userId }

        assertThat(users, containsInAnyOrder(*(testUsersWithoutDeletedOne.toTypedArray())))

        userRepository.restoreUser(UnusedUser.userId)

        users = userRepository.getUsers()
        assertThat(users, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray())))
    }

    @Test
    fun edit_user() {
        val editedUserBoxi =
            User(Berufsbildner.userId, UserName("boxi_new"), Berufsbildner.email, Berufsbildner.role)

        userRepository.editUser(editedUserBoxi.userId, editedUserBoxi)

        val user = userRepository.getUser(Berufsbildner.userId)

        assertThat(user.userName, `is`(editedUserBoxi.userName))
    }

    @Test
    fun delete_deletedUser() {
        val deletedUser = userRepository.deleteDeletedUser(DeletedUser.userId)

        assertThat(deletedUser, `is`(deletedUser))

        val deletedTable = userRepository.getDeletedUsers()

        assertThat(deletedTable, empty())
    }

    // Connections

    @Test
    fun load_all_connections_for_supervisor() {
        val result = userRepository.getApprenticeSupervisorPairs(Berufsbildner.userId)

        assertThat(result, hasSize(1))
        assertThat(result, containsInAnyOrder(TestConnections.connection1))
    }

    @Test
    fun load_all_apprentice_supervisor_connections() {
        val result = userRepository.getApprenticeSupervisorPairs()

        assertThat(result, containsInAnyOrder(*(TestConnections.connections.toTypedArray())))
    }

    @Test
    fun connect_apprentice_to_supervisor() {
        val connection = userRepository.connectApprenticeToSupervisor(Lernender.userId, Admin.userId)

        val result = userRepository.getApprenticeSupervisorPairs()

        assertThat(
            result,
            containsInAnyOrder(
                *(TestConnections.connections.toTypedArray()),
                ApprenticeSupervisorPair(connection.id, Lernender, Admin, false),
            ),
        )
    }

    @Test
    fun delete_apprenticeSupervisor_connection() {
        userRepository.deleteConnection(TestConnections.connection1.id)

        val result = userRepository.getApprenticeSupervisorPairs()

        val connectionListWithoutDeletedOne =
            TestConnections.connections.filter { it.id != TestConnections.connection1.id }

        assertThat(result, containsInAnyOrder(*(connectionListWithoutDeletedOne.toTypedArray())))
    }

    @Test
    fun update_connection() {
        val updatedConnection = TestConnections.connection2.copy(emailNotification = true)
        userRepository.updateOrInsertApprenticeSupervisorConnection(updatedConnection)

        val result = userRepository.getApprenticeSupervisorPairs(updatedConnection.supervisor.userId)

        assertThat(result, containsInAnyOrder(updatedConnection))
    }

    @Test
    fun update_connection_will_insert_new_entry_if_it_does_not_exist() {
        val newConnection =
            ApprenticeSupervisorPair(
                id = "78418690-ef7f-4ec2-83f5-970de80da37c",
                apprentice = Lernender,
                supervisor = Noah,
                emailNotification = true,
            )

        userRepository.updateOrInsertApprenticeSupervisorConnection(newConnection)

        val result = userRepository.getApprenticeSupervisorPairs(newConnection.supervisor.userId)
        assertThat(result, containsInRelativeOrder(newConnection))
    }
}
