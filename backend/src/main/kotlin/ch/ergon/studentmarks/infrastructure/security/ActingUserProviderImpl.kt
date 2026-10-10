package ch.ergon.studentmarks.infrastructure.security

import ch.ergon.studentmarks.application.security.ActingPerson
import ch.ergon.studentmarks.application.security.ActingUser
import ch.ergon.studentmarks.application.security.ActingUserProvider
import ch.ergon.studentmarks.application.security.Job
import ch.ergon.studentmarks.infrastructure.security.authentication.JobAuthentication
import ch.ergon.studentmarks.infrastructure.security.authentication.PersonAuthentication
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class ActingUserProviderImpl : ActingUserProvider {
    private val currentAuthentication: Authentication?
        get() = SecurityContextHolder.getContext().authentication

    override val optionalActingUser: ActingUser?
        get() =
            when (val authentication = currentAuthentication) {
                is PersonAuthentication -> authentication.actingPerson
                is JobAuthentication -> Job
                else -> null
            }

    override val actingPerson: ActingPerson
        get() =
            optionalActingUser as? ActingPerson
                ?: error("Cannot determine ActingPerson, unexpected authentication token $currentAuthentication")
}
