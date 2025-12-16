package ch.ergon.studentmarks.infrastructure.security.authentication

import ch.ergon.studentmarks.infrastructure.security.exeptions.MailNotInSubjectException
import org.springframework.stereotype.Service

@Service
class SubjectExtractionService {
    fun extractMailFromSubject(subject: String): String {
        val regex = "emailAddress=([^,]+)".toRegex()
        val matchResult = regex.find(subject)
        return matchResult?.groups?.get(1)?.value
            ?: throw MailNotInSubjectException()
    }
}
