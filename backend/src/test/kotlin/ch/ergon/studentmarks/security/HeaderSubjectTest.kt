package ch.ergon.studentmarks.security

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.infrastructure.security.authentication.SubjectExtractionService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class HeaderSubjectTest : IntegrationTest() {
    @Autowired
    private lateinit var subjectExtractionService: SubjectExtractionService

    @Test
    fun `Should extract email from subject string`() {
        val subjectString =
            "emailAddress=max.muster@ergon.ch,CN=Max Muster,OU=IT,O=Ergon Informatik AG,L=Zuerich,ST=Zuerich,C=CH"

        val email = subjectExtractionService.extractMailFromSubject(subjectString)

        assertThat(email, `is`("max.muster@ergon.ch"))
    }
}
