package ch.ergon.studentmarks.infrastructure.logging

import ch.ergon.studentmarks.infrastructure.REQUEST_LOGGER_ORDER
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Order(REQUEST_LOGGER_ORDER)
@Component
class RequestLoggingFilter : OncePerRequestFilter() {
    companion object : Logger

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // log the ingoing call
        val stoppWatch = StopWatch()
        stoppWatch.start()
        val method = request.method
        val uri = request.requestURI
        LOG.info("Incoming $method request to $uri")

        // call the next filter
        filterChain.doFilter(request, response)

        // after everything is done log the response
        val responseCode = HttpStatus.valueOf(response.status)
        stoppWatch.stop()
        val totalTimeMillis = stoppWatch.totalTimeMillis
        LOG.info("Incoming $method response to $uri returned in ${totalTimeMillis}ms with code $responseCode")
    }
}
