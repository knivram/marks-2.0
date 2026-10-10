package ch.ergon.studentmarks.security

import ch.ergon.studentmarks.application.security.ActingPerson
import ch.ergon.studentmarks.application.security.ActingUserProvider
import ch.ergon.studentmarks.application.security.HasMarkEditPermission
import ch.ergon.studentmarks.application.security.HasMarkPurgePermission
import ch.ergon.studentmarks.application.security.HasStudentDataReadPermission
import ch.ergon.studentmarks.application.security.IsAdmin
import ch.ergon.studentmarks.application.security.MarkIdTargetResolver
import ch.ergon.studentmarks.application.security.TargetId
import ch.ergon.studentmarks.application.security.TargetUserResolver
import ch.ergon.studentmarks.application.security.UserIdTargetResolver
import ch.ergon.studentmarks.domain.exception.MarkNotFoundException
import ch.ergon.studentmarks.domain.exception.UserNotFoundException
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.Role
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.model.user.UserName
import ch.ergon.studentmarks.domain.repository.ApprenticeSupervisorRepository
import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.domain.repository.UserRepository
import ch.ergon.studentmarks.infrastructure.security.ActingUserProviderImpl
import ch.ergon.studentmarks.infrastructure.security.GrantedToAuthorizationConfiguration
import ch.ergon.studentmarks.infrastructure.security.authentication.JobAuthentication
import ch.ergon.studentmarks.infrastructure.security.authentication.PersonAuthentication
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class GrantedToAuthorizationManagerTest {
    private val apprentice = user(Role.APPRENTICE)
    private val otherApprentice = user(Role.APPRENTICE)
    private val supervisor = user(Role.SUPERVISOR)
    private val admin = user(Role.ADMIN)

    private val apprenticeMark = MarkId(UUID.randomUUID())
    private val otherApprenticeMark = MarkId(UUID.randomUUID())
    private val unknownMark = MarkId(UUID.randomUUID())
    private val unknownUser = UserId(UUID.randomUUID())

    private val apprenticeSupervisorRepository =
        mock<ApprenticeSupervisorRepository>().also {
            whenever(it.isConnected(any(), any())).thenReturn(false)
            whenever(it.isConnected(apprentice.userId, supervisor.userId)).thenReturn(true)
        }

    private val userRepository =
        mock<UserRepository>().also { repository ->
            listOf(apprentice, otherApprentice, supervisor, admin).forEach { whenever(repository.getById(it.userId)).thenReturn(it) }
            whenever(repository.getById(unknownUser)).thenThrow(UserNotFoundException(unknownUser.value.toString()))
        }

    private val markRepository =
        mock<MarkRepository>().also {
            whenever(it.getOwnerUserId(apprenticeMark)).thenReturn(apprentice.userId)
            whenever(it.getOwnerUserId(otherApprenticeMark)).thenReturn(otherApprentice.userId)
            whenever(it.getOwnerUserId(unknownMark)).thenThrow(MarkNotFoundException(unknownMark))
        }

    private val secured: SecuredTarget = securedProxy()

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun apprentice_can_edit_own_mark() {
        actingAs(apprentice)
        assertThat(secured.editMark(apprenticeMark), `is`("ok"))
    }

    @Test
    fun apprentice_cannot_edit_mark_of_other_apprentice() {
        actingAs(apprentice)
        assertThrows<AccessDeniedException> { secured.editMark(otherApprenticeMark) }
    }

    @Test
    fun unknown_mark_is_not_found() {
        actingAs(apprentice)
        assertThrows<MarkNotFoundException> { secured.editMark(unknownMark) }
    }

    @Test
    fun supervisor_cannot_edit_mark_of_own_apprentice_and_target_is_never_resolved() {
        actingAs(supervisor)
        assertThrows<AccessDeniedException> { secured.editMark(apprenticeMark) }
        verifyNoInteractions(markRepository)
    }

    @Test
    fun supervisor_can_read_self_and_connected_apprentice() {
        actingAs(supervisor)
        assertThat(secured.read(supervisor.userId), `is`("ok"))
        assertThat(secured.read(apprentice.userId), `is`("ok"))
    }

    @Test
    fun supervisor_cannot_read_unconnected_apprentice() {
        actingAs(supervisor)
        assertThrows<AccessDeniedException> { secured.read(otherApprentice.userId) }
    }

    @Test
    fun apprentice_cannot_read_other_apprentice() {
        actingAs(apprentice)
        assertThrows<AccessDeniedException> { secured.read(otherApprentice.userId) }
    }

    @Test
    fun admin_can_read_anyone_but_cannot_write_marks() {
        actingAs(admin)
        assertThat(secured.read(otherApprentice.userId), `is`("ok"))
        assertThrows<AccessDeniedException> { secured.editMark(otherApprenticeMark) }
        verifyNoInteractions(markRepository)
    }

    @Test
    fun admin_reading_unknown_user_is_not_found() {
        actingAs(admin)
        assertThrows<UserNotFoundException> { secured.read(unknownUser) }
    }

    @Test
    fun missing_target_is_denied_even_for_admin() {
        actingAs(admin)
        assertThrows<AccessDeniedException> { secured.read(null) }
    }

    @Test
    fun only_admin_passes_is_admin() {
        actingAs(admin)
        assertThat(secured.adminOnly(), `is`("ok"))

        actingAs(supervisor)
        assertThrows<AccessDeniedException> { secured.adminOnly() }
    }

    @Test
    fun job_passes_technical_permission_but_no_user_permission() {
        actingAs(JobAuthentication)
        assertThat(secured.purge(), `is`("ok"))
        assertThrows<AccessDeniedException> { secured.read(apprentice.userId) }
        assertThrows<AccessDeniedException> { secured.adminOnly() }
    }

    @Test
    fun users_do_not_pass_technical_permission() {
        actingAs(admin)
        assertThrows<AccessDeniedException> { secured.purge() }
    }

    @Test
    fun unauthenticated_caller_is_denied() {
        assertThrows<AccessDeniedException> { secured.adminOnly() }
    }

    private fun actingAs(user: User) = actingAs(PersonAuthentication(ActingPerson(user)))

    private fun actingAs(authentication: Authentication) {
        SecurityContextHolder.getContext().authentication = authentication
    }

    private fun securedProxy(): SecuredTarget {
        val beanFactory =
            DefaultListableBeanFactory().apply {
                registerSingleton("actingUserProvider", ActingUserProviderImpl())
                registerSingleton("apprenticeSupervisorRepository", apprenticeSupervisorRepository)
                registerSingleton("userIdTargetResolver", UserIdTargetResolver(userRepository))
                registerSingleton("markIdTargetResolver", MarkIdTargetResolver(markRepository))
            }
        val interceptor =
            GrantedToAuthorizationConfiguration.grantedToAuthorizationInterceptor(
                beanFactory.getBeanProvider(ActingUserProvider::class.java),
                beanFactory.getBeanProvider(ApprenticeSupervisorRepository::class.java),
                beanFactory.getBeanProvider(TargetUserResolver::class.java),
            )
        return ProxyFactory(SecuredTarget())
            .apply {
                isProxyTargetClass = true
                addAdvisor(interceptor)
            }.proxy as SecuredTarget
    }

    private fun user(role: Role): User {
        val id = UUID.randomUUID()
        return User(UserId(id), UserName("user-$id"), Email.create("$id@ergon.ch"), role)
    }

    open class SecuredTarget {
        @HasMarkEditPermission
        open fun editMark(
            @TargetId markId: MarkId,
        ) = "ok"

        @HasStudentDataReadPermission
        open fun read(
            @TargetId userId: UserId?,
        ) = "ok"

        @IsAdmin
        open fun adminOnly() = "ok"

        @HasMarkPurgePermission
        open fun purge() = "ok"
    }
}
