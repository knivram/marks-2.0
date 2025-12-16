package ch.ergon.studentmarks.infrastructure.security.authentication

import ch.ergon.studentmarks.domain.user.User
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider : AuthenticationProvider {
    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication {
        val user: User = authentication.principal as User
        val grantedAuthorities = mutableListOf(SimpleGrantedAuthority("ROLE_USER"))
        if (user.isAdmin) {
            grantedAuthorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
        }
        return CustomAuthentication(user, grantedAuthorities)
    }

    override fun supports(authentication: Class<*>): Boolean = CustomAuthentication::class.java == authentication
}
