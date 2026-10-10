package ch.ergon.studentmarks.infrastructure.security.authentication

import ch.ergon.studentmarks.application.security.ActingUserProvider
import ch.ergon.studentmarks.infrastructure.logging.LogbackKeys.REQUEST_USER_ID
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class UserLogFilter(
    private val actingUserProvider: ActingUserProvider,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val userName = actingUserProvider.optionalActingUser?.name
        if (userName != null) {
            MDC.put(REQUEST_USER_ID.name, userName)
        } else {
            MDC.remove(REQUEST_USER_ID.name)
        }
        filterChain.doFilter(request, response)
    }
}
