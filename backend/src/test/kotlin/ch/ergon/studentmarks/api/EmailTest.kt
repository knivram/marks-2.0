package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.application.service.EmailNotificationService
import ch.ergon.studentmarks.application.service.MarkService
import ch.ergon.studentmarks.application.service.UserService
import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.domain.repository.SubjectRepository
import ch.ergon.studentmarks.infrastructure.email.EmailSender
import ch.ergon.studentmarks.security.context.WithMockLehrling
import ch.ergon.studentmarks.test.objects.TestMarks
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Berufsbildner
import ch.ergon.studentmarks.utils.TimeUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.contains
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.thymeleaf.TemplateEngine
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EmailTest : IntegrationTest() {
    private lateinit var emailNotificationService: EmailNotificationService

    private lateinit var emailSender: EmailSender

    private lateinit var markService: MarkService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var markRepository: MarkRepository

    @Autowired
    private lateinit var subjectRepository: SubjectRepository

    @Autowired
    private lateinit var timeUtils: TimeUtils

    @Autowired
    private lateinit var templateEngine: TemplateEngine

    @BeforeEach
    fun initMockData() {
        emailSender = mock(EmailSender::class.java)
        emailNotificationService =
            EmailNotificationService(
                templateEngine,
                userService,
                emailSender,
            )
        markService =
            MarkService(
                markRepository = markRepository,
                subjectRepository = subjectRepository,
                userService = userService,
                timeUtils = timeUtils,
                emailNotificationService = emailNotificationService,
            )
    }

    @Test
    @WithMockLehrling
    fun new_mark_with_value_will_send_notification() {
        val mark =
            Mark(
                MarkId("f67ce504-3436-4c4c-babe-42374d1b50d0"),
                "New Mark",
                TestSubjects.English.id,
                LocalDate.parse("2023-01-10"),
                1000,
                MarkValue(5450),
                SemesterId("e9b6a70e-87cd-464b-97a6-bd15c611bcdd"),
            )
        markService.addNewMark(mark)

        verify(emailSender).sendMail(eq("Neue Note"), anyString(), anyList())
    }

    @Test
    @WithMockLehrling
    fun new_mark_without_value_wont_send_notification() {
        val mark =
            Mark(
                MarkId("f67ce504-3436-4c4c-babe-42374d1b50d0"),
                "New Mark",
                TestSubjects.English.id,
                LocalDate.parse("2023-01-10"),
                1000,
                MarkValue(null),
                SemesterId("e9b6a70e-87cd-464b-97a6-bd15c611bcdd"),
            )
        markService.addNewMark(mark)

        verifyNoInteractions(emailSender)
    }

    @Test
    @WithMockLehrling
    fun edite_mark_will_send_email_notification() {
        val newMark = TestMarks.Companion.SpecialMarks.EditedMathMark
        markService.editMark(newMark)

        verify(emailSender).sendMail(eq("Note geändert"), anyString(), anyList())
    }
}

private fun LocalDate.format() = format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

private fun Int.format() = BigDecimal(this.toDouble() / 1000).setScale(2, RoundingMode.DOWN).toPlainString()
