package ch.ergon.studentmarks.infrastructure.security.authentication

import ch.ergon.studentmarks.application.security.ActingPerson
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * A request authenticated through the reverse proxy's subject header.
 */
class PersonAuthentication(
    val actingPerson: ActingPerson,
) : AbstractAuthenticationToken(emptyList<GrantedAuthority>()) {
    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): Any = actingPerson

    override fun getName(): String = actingPerson.name
}

/**
 * Scheduled and asynchronous work, which has no person behind it.
 */
object JobAuthentication : AbstractAuthenticationToken(emptyList<GrantedAuthority>()) {
    init {
        isAuthenticated = true
    }

    override fun getCredentials(): Any? = null

    override fun getPrincipal(): Any = "JOB"
}
