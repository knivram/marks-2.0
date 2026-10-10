package ch.ergon.studentmarks.repository

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.Role
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserName
import ch.ergon.studentmarks.domain.repository.ApprenticeSupervisorRepository
import ch.ergon.studentmarks.domain.repository.UserRepository
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

    @Autowired
    private lateinit var apprenticeSupervisorRepository: ApprenticeSupervisorRepository

    @Test
    fun getUser() {
        val result = userRepository.getByEmail(Lernender.email)

        assertThat(result, `is`(Lernender))
    }

    @Test
    fun supervisorListOfApprenticeContainsAnApprentices() {
        val result = userRepository.findApprenticesForSupervisor(Berufsbildner)

        assertThat(result, hasSize(1))
        assertThat(result, containsInAnyOrder(Lernender))
    }

    @Test
    fun list_of_supervisor_contains_the_supervisor_of_the_selected_apprentice() {
        val result = userRepository.findSupervisorsWithNotificationsForApprentice(Lernender.userId)

        assertThat(result, hasSize(1))
        assertThat(result, containsInAnyOrder(Berufsbildner))
    }

    @Test
    fun list_of_supervisor_contains_all_supervisors_of_an_apprentice_only_once() {
        val result = userRepository.findSupervisorsWithNotificationsForApprentice(Kevin.userId)

        assertThat(result, hasSize(1))
        assertThat(result, containsInAnyOrder(Noah))
    }

    @Test
    fun list_of_apprentices_is_empty_for_apprentices() {
        val result = userRepository.findApprenticesForSupervisor(Lernender)

        assertThat(result, empty())
    }

    @Test
    fun search_for_apprentice_without_query_returns_list_of_all_apprentice_mail() {
        val result = userRepository.searchApprentices("")

        assertThat(result, containsInAnyOrder(*(TestUser.Apprentices.toTypedArray())))
    }

    @Test
    fun search_for_apprentice_with_query_Marvin_returns_only_one_mail() {
        val result = userRepository.searchApprentices("Marvin")

        assertThat(result, containsInAnyOrder(Lernender))
    }

    @Test
    fun search_for_supervisor_without_query_returns_list_of_all_supervisor_mail() {
        val result = userRepository.searchSupervisors("")

        assertThat(result, containsInAnyOrder(*(TestUser.Supervisors.toTypedArray())))
    }

    @Test
    fun search_for_supervisor_with_query_Boksberger_returns_only_one_mail() {
        val result = userRepository.searchSupervisors("Boksberger")

        assertThat(
            result,
            containsInAnyOrder(Berufsbildner),
        )
    }

    @Test
    fun get_all_users() {
        val result = userRepository.getAll()
        assertThat(result, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray())))
    }

    @Test
    fun create_new_user() {
        val newUser = User.create(Email.create("marvin@ergon.ch"), UserName("kmarvin"), Role.APPRENTICE)
        val user = userRepository.create(newUser)
        val result = userRepository.getAll()

        assertThat(result, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray()), user))
    }

    @Test
    fun get_deleted_user_returns_all_deleted_users() {
        val deletedUsers = userRepository.findDeleted()
        assertThat(deletedUsers, containsInAnyOrder(DeletedUser))
    }

    @Test
    fun delete_user() {
        val usersBefore = userRepository.getAll()
        assertThat(usersBefore, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray())))

        userRepository.softDelete(UnusedUser.userId)

        val users = userRepository.getAll()
        val deletedUsers = userRepository.findDeleted()
        val testUsersWithoutDeletedOne = TestUser.AllUsers.filter { it.userId != UnusedUser.userId }

        assertThat(users, containsInAnyOrder(*(testUsersWithoutDeletedOne.toTypedArray())))
        assertThat(deletedUsers, containsInAnyOrder(DeletedUser, UnusedUser))
    }

    @Test
    fun after_deletion_user_is_in_deleted_Table() {
        userRepository.softDelete(UnusedUser.userId)

        val deletedUsers = userRepository.findDeleted()

        assertThat(deletedUsers, containsInAnyOrder(DeletedUser, UnusedUser))
    }

    @Test
    fun restore_deleted_user() {
        userRepository.restore(DeletedUser.userId)

        val deletedUsers = userRepository.findDeleted()
        assertThat(deletedUsers, empty())

        val users = userRepository.getAll()
        assertThat(users, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray()), DeletedUser))
    }

    @Test
    fun delete_and_restore_after() {
        var users = userRepository.getAll()
        assertThat(users, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray())))

        userRepository.softDelete(UnusedUser.userId)

        users = userRepository.getAll()

        val testUsersWithoutDeletedOne = TestUser.AllUsers.filter { it.userId != UnusedUser.userId }

        assertThat(users, containsInAnyOrder(*(testUsersWithoutDeletedOne.toTypedArray())))

        userRepository.restore(UnusedUser.userId)

        users = userRepository.getAll()
        assertThat(users, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray())))
    }

    @Test
    fun edit_user() {
        val editedUserBoxi =
            User(Berufsbildner.userId, UserName("boxi_new"), Berufsbildner.email, Berufsbildner.role)

        userRepository.update(editedUserBoxi.userId, editedUserBoxi)

        val user = userRepository.getById(Berufsbildner.userId)

        assertThat(user.userName, `is`(editedUserBoxi.userName))
    }

    @Test
    fun delete_deletedUser() {
        val deletedUser = userRepository.permanentlyDelete(DeletedUser.userId)

        assertThat(deletedUser, `is`(deletedUser))

        val deletedTable = userRepository.findDeleted()

        assertThat(deletedTable, empty())
    }

    // Connections

    @Test
    fun load_all_connections_for_supervisor() {
        val result = apprenticeSupervisorRepository.findBySupervisor(Berufsbildner.userId, false)

        assertThat(result, hasSize(1))
        assertThat(result, containsInAnyOrder(TestConnections.connection1))
    }

    @Test
    fun load_all_apprentice_supervisor_connections() {
        val result = apprenticeSupervisorRepository.getAll()

        assertThat(result, containsInAnyOrder(*(TestConnections.connections.toTypedArray())))
    }

    @Test
    fun connect_apprentice_to_supervisor() {
        val connection = apprenticeSupervisorRepository.connect(Lernender.userId, Admin.userId)

        val result = apprenticeSupervisorRepository.getAll()

        assertThat(
            result,
            containsInAnyOrder(
                *(TestConnections.connections.toTypedArray()),
                ApprenticeSupervisorPair(connection.id, Lernender.userId, Admin.userId, false),
            ),
        )
    }

    @Test
    fun delete_apprenticeSupervisor_connection() {
        apprenticeSupervisorRepository.delete(TestConnections.connection1.id)

        val result = apprenticeSupervisorRepository.getAll()

        val connectionListWithoutDeletedOne =
            TestConnections.connections.filter { it.id != TestConnections.connection1.id }

        assertThat(result, containsInAnyOrder(*(connectionListWithoutDeletedOne.toTypedArray())))
    }

    @Test
    fun update_connection() {
        val updatedConnection = TestConnections.connection2.copy(emailNotification = true)
        apprenticeSupervisorRepository.updateOrInsert(updatedConnection)

        val result = apprenticeSupervisorRepository.findBySupervisor(updatedConnection.supervisorId, false)

        assertThat(result, containsInAnyOrder(updatedConnection))
    }

    @Test
    fun update_connection_will_insert_new_entry_if_it_does_not_exist() {
        val newConnection =
            ApprenticeSupervisorPair(
                id = "78418690-ef7f-4ec2-83f5-970de80da37c",
                apprenticeId = Lernender.userId,
                supervisorId = Noah.userId,
                emailNotification = true,
            )

        apprenticeSupervisorRepository.updateOrInsert(newConnection)

        val result = apprenticeSupervisorRepository.findBySupervisor(newConnection.supervisorId, false)
        assertThat(result, containsInRelativeOrder(newConnection))
    }
}
