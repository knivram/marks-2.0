package ch.ergon.studentmarks.infrastructure.security.authentication

import ch.ergon.studentmarks.application.security.ActingPerson
import ch.ergon.studentmarks.domain.exception.MailNotInSubjectException
import ch.ergon.studentmarks.domain.exception.UserNotFoundException
import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.repository.UserRepository
import ch.ergon.studentmarks.infrastructure.logging.Logger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

const val SUBJECT_HEADER_NAME = "x-forwarded-subject"

/**
 * Authenticates a request from the client certificate subject the reverse proxy forwards: the subject's email
 * identifies the user, who becomes the [PersonAuthentication] of the request.
 */
@Component
class SubjectHeaderAuthenticationFilter(
    private val userRepository: UserRepository,
    private val subjectExtractionService: SubjectExtractionService,
) : OncePerRequestFilter() {
    companion object : Logger

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        val urlEncodedSubject = request.getHeader(SUBJECT_HEADER_NAME)
        if (urlEncodedSubject == null) {
            LOG.info("No subject in request")
            response.status = HttpStatus.UNAUTHORIZED.value()
            return
        }

        val user =
            try {
                val subject = URLDecoder.decode(urlEncodedSubject, StandardCharsets.UTF_8)
                val email = subjectExtractionService.extractMailFromSubject(subject)
                userRepository.getByEmail(Email.create(email))
            } catch (_: IllegalArgumentException) {
                LOG.info("Subject is not valid URL encoding")
                response.status = HttpStatus.UNAUTHORIZED.value()
                return
            } catch (_: MailNotInSubjectException) {
                LOG.info("No email in subject")
                response.status = HttpStatus.UNAUTHORIZED.value()
                return
            } catch (e: UserNotFoundException) {
                LOG.info("User with email: ${e.searchParam} not found")
                response.status = HttpStatus.UNAUTHORIZED.value()
                return
            }

        SecurityContextHolder.getContext().authentication = PersonAuthentication(ActingPerson(user))
        chain.doFilter(request, response)
    }
}
