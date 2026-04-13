package ch.ergon.studentmarks.infrastructure.security.authentication

import ch.ergon.studentmarks.domain.model.user.User
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class CustomAuthentication(
    user: User,
    authorities: Collection<GrantedAuthority>,
) : Authentication {
    private val user: User
    private val authorities: Collection<GrantedAuthority>

    constructor(user: User) : this(user, ArrayList<GrantedAuthority>())

    init {
        this.user = user
        this.authorities = authorities
    }

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getCredentials(): Any? = null

    override fun getDetails(): Any? = null

    override fun getPrincipal(): Any = user

    override fun isAuthenticated(): Boolean = true

    @Throws(IllegalArgumentException::class)
    override fun setAuthenticated(isAuthenticated: Boolean) {
    }

    override fun getName(): String = user.userName.value
}
