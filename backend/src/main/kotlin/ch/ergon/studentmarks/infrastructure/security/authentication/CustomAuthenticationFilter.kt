package ch.ergon.studentmarks.infrastructure.security.authentication

import ch.ergon.studentmarks.domain.user.Email
import ch.ergon.studentmarks.domain.user.UserService
import ch.ergon.studentmarks.infrastructure.logging.Logger
import ch.ergon.studentmarks.infrastructure.security.exeptions.MailNotInSubjectException
import ch.ergon.studentmarks.infrastructure.security.exeptions.StudentMarksException
import ch.ergon.studentmarks.infrastructure.security.exeptions.SubjectNotInHeaderException
import ch.ergon.studentmarks.infrastructure.security.exeptions.UserNotFoundException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

const val SUBJECT_HEADER_NAME = "x-forwarded-subject"

@Component
class CustomAuthenticationFilter(
    @Lazy private var manager: AuthenticationManager,
    private val userService: UserService,
    private val subjectExtractionService: SubjectExtractionService,
) : OncePerRequestFilter() {
    companion object : Logger

    private fun HttpServletResponse.logAndRespond(
        message: String,
        status: HttpStatus,
    ) {
        LOG.info(message)
        this.status = status.value()
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        try {
            processAuthentication(request, response, chain)
        } catch (e: StudentMarksException) {
            when (e) {
                is MailNotInSubjectException ->
                    response.logAndRespond("No email in subject", HttpStatus.UNAUTHORIZED)
                is SubjectNotInHeaderException ->
                    response.logAndRespond("No subject in request", HttpStatus.UNAUTHORIZED)
                is UserNotFoundException ->
                    response.logAndRespond("User with email: ${e.searchParam} not found", HttpStatus.UNAUTHORIZED)
            }
        } catch (e: Exception) {
            response.logAndRespond("Authentication failed", HttpStatus.FORBIDDEN)
            LOG.error("Authentication failed", e)
        }
    }

    private fun processAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val urlEncodedSubject = request.getHeader(SUBJECT_HEADER_NAME) ?: throw SubjectNotInHeaderException()
        val subject = URLDecoder.decode(urlEncodedSubject, StandardCharsets.UTF_8.toString())

        val email = subjectExtractionService.extractMailFromSubject(subject)

        val user = userService.getUserByEmail(Email(email.lowercase()))
        val customAuthentication = CustomAuthentication(user)

        val result = manager.authenticate(customAuthentication)
        if (result.isAuthenticated) {
            SecurityContextHolder.getContext().authentication = result
            chain.doFilter(request, response)
        }
    }
}
