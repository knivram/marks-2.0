package ch.ergon.studentmarks.security

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.application.service.MarkService
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.infrastructure.email.EmailSender
import ch.ergon.studentmarks.security.context.WithMockAdmin
import ch.ergon.studentmarks.security.context.WithMockLehrling
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Berufsbildner
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate

/**
 * Background work runs as the job actor, through the real proxies: the authorization check of an `@Async` method
 * runs on the executor thread, not on the caller's.
 */
class JobActorTest : IntegrationTest() {
    @MockitoBean
    private lateinit var emailSender: EmailSender

    @Autowired
    private lateinit var markService: MarkService

    @Test
    @WithMockLehrling
    fun adding_a_mark_notifies_the_supervisor_as_job() {
        markService.addNewMark(
            userId = Lernender.userId,
            name = "New Mark",
            subjectId = TestSubjects.English.id,
            date = LocalDate.parse("2023-01-10"),
            weight = 1000,
            value = MarkValue(5450),
            semesterId = SemesterId("e9b6a70e-87cd-464b-97a6-bd15c611bcdd"),
        )

        verify(emailSender, timeout(5_000)).sendMail(eq("Neue Note"), any(), eq(listOf(Berufsbildner)))
    }

    @Test
    @WithMockAdmin
    fun users_cannot_trigger_the_mark_purge() {
        assertThrows<AccessDeniedException> { markService.deleteAllExpiredMarks() }
    }
}
