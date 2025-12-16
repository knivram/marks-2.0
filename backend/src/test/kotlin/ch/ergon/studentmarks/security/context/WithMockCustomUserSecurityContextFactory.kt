package ch.ergon.studentmarks.security.context

import ch.ergon.studentmarks.domain.user.Email
import ch.ergon.studentmarks.domain.user.UserService
import ch.ergon.studentmarks.infrastructure.security.authentication.CustomAuthentication
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithMockCustomUserSecurityContextFactory(
    @Lazy private var manager: AuthenticationManager,
    private val userService: UserService,
) : WithSecurityContextFactory<WithMockCustomUser> {
    override fun createSecurityContext(customUser: WithMockCustomUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val customAuthentication = CustomAuthentication(userService.getUserByEmail(Email(customUser.email)))
        val auth: Authentication = manager.authenticate(customAuthentication)
        context.authentication = auth
        return context
    }
}
