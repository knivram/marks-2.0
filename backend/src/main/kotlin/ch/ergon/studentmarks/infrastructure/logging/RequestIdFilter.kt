package ch.ergon.studentmarks.infrastructure.logging

import ch.ergon.studentmarks.infrastructure.REQUEST_ID_ORDER
import ch.ergon.studentmarks.infrastructure.logging.LogbackKeys.REQUEST_ID
import ch.ergon.tablero.infrastructure.IdGenerator
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Order(REQUEST_ID_ORDER)
@Component
class RequestIdFilter : OncePerRequestFilter() {
    companion object : Logger {
        val ID_GENERATOR = IdGenerator()
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        MDC.put(REQUEST_ID.name, ID_GENERATOR.generate())
        filterChain.doFilter(request, response)
    }
}
