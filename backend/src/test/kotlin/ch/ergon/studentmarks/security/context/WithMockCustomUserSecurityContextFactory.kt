package ch.ergon.studentmarks.security.context

import ch.ergon.studentmarks.application.security.ActingPerson
import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.repository.UserRepository
import ch.ergon.studentmarks.infrastructure.security.authentication.PersonAuthentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory

class WithMockCustomUserSecurityContextFactory(
    private val userRepository: UserRepository,
) : WithSecurityContextFactory<WithMockCustomUser> {
    override fun createSecurityContext(customUser: WithMockCustomUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = PersonAuthentication(ActingPerson(userRepository.getByEmail(Email.create(customUser.email))))
        return context
    }
}
