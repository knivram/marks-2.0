package ch.ergon.studentmarks.service

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.application.service.UserService
import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.domain.repository.UserRepository
import ch.ergon.studentmarks.security.context.WithMockAdmin
import ch.ergon.studentmarks.security.context.WithMockBerufsbildner
import ch.ergon.studentmarks.security.context.WithMockLehrling
import ch.ergon.studentmarks.test.objects.TestConnections
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.UnusedUserMarks
import ch.ergon.studentmarks.test.objects.TestUser
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Admin
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Apprentices
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Berufsbildner
import ch.ergon.studentmarks.test.objects.TestUser.Companion.DeletedUser
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import ch.ergon.studentmarks.test.objects.TestUser.Companion.UnusedUser
import ch.ergon.studentmarks.utils.TestUuidUtilsImpl
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class UserServiceTest : IntegrationTest() {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var markRepository: MarkRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var uuidUtilsImpl: TestUuidUtilsImpl

    @Test
    @WithMockAdmin
    fun admin_gets_all_apprentice() {
        val result = userService.getApprentice(Admin.userId)

        assertThat(result, containsInAnyOrder(*(Apprentices.toTypedArray())))
    }

    @Test
    @WithMockBerufsbildner
    fun supervisor_gets_his_apprentice() {
        val result = userService.getApprentice(Berufsbildner.userId)

        assertThat(result, hasSize(1))
        assertThat(result, contains(Lernender))
    }

    @Test
    @WithMockLehrling
    fun getCurrentUser_returns_current_user() {
        val currentUser = userService.getCurrentUser()

        assertThat(currentUser, `is`(Lernender))
    }

    @Test
    @WithMockAdmin
    fun restore_user_also_restores_his_marks() {
        userService.deleteUser(UnusedUser.userId)
        userService.restoreUser(UnusedUser.userId)

        // check if deleted user and marks are deleted form delete table
        val deletedMarks = markRepository.findDeleted(UnusedUser.userId)
        assertThat(deletedMarks, empty())

        val deletedUser = userRepository.findDeleted()
        assertThat(deletedUser, containsInAnyOrder(DeletedUser))

        // User and Marks should be back
        val marks = markRepository.findByUser(UnusedUser.userId)
        assertThat(marks, containsInAnyOrder(*(UnusedUserMarks.marks.toTypedArray())))

        val users = userRepository.findAll()

        assertThat(users, containsInAnyOrder(*(TestUser.AllUsers.toTypedArray())))
    }

    @Test
    @WithMockAdmin
    fun get_apprentice_supervisor_connections_for_supervisor_returns_all_apprentice_for_admin() {
        uuidUtilsImpl.setUUID(UUID.fromString("c0f33502-c20c-4728-a897-ccb85101532a"))
        val connections = userService.getApprenticeSupervisorPairs(Admin.userId)

        assertThat(connections, hasSize(TestConnections.allAdminConnections.size))
        assertThat(connections, containsInAnyOrder(*(TestConnections.allAdminConnections.toTypedArray())))
    }

    @Test
    @WithMockBerufsbildner
    fun get_apprentice_supervisor_connections_for_supervisor() {
        val connections = userService.getApprenticeSupervisorPairs(Berufsbildner.userId)

        assertThat(connections, hasSize(1))
        assertThat(connections, containsInAnyOrder(TestConnections.connection1))
    }
}
