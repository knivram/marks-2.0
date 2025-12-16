package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.db.MarkRepository
import ch.ergon.studentmarks.db.SubjectsRepository
import ch.ergon.studentmarks.domain.email.EmailSender
import ch.ergon.studentmarks.domain.email.EmailService
import ch.ergon.studentmarks.domain.marks.Mark
import ch.ergon.studentmarks.domain.marks.MarkId
import ch.ergon.studentmarks.domain.marks.MarkService
import ch.ergon.studentmarks.domain.marks.MarkValue
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.user.UserService
import ch.ergon.studentmarks.security.context.WithMockLehrling
import ch.ergon.studentmarks.test.objects.TestMarks
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Berufsbildner
import ch.ergon.studentmarks.utils.TimeUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.contains
import org.mockito.Mockito.mock
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
    private lateinit var emailService: EmailService

    private lateinit var emailSender: EmailSender

    private lateinit var markService: MarkService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var marksRepository: MarkRepository

    @Autowired
    private lateinit var subjectsRepository: SubjectsRepository

    @Autowired
    private lateinit var timeUtils: TimeUtils

    @Autowired
    private lateinit var templateEngine: TemplateEngine

    @BeforeEach
    fun initMockData() {
        emailSender = mock(EmailSender::class.java)
        emailService =
            EmailService(
                templateEngine,
                userService,
                emailSender,
            )
        markService =
            MarkService(
                markRepository = marksRepository,
                emailService = emailService,
                subjectsRepository = subjectsRepository,
                timeUtils = timeUtils,
                userService = userService,
            )
    }

    @Test
    @WithMockLehrling
    fun new_mark_with_value_will_send_notification() {
        val mark =
            Mark(
                MarkId("f67ce504-3436-4c4c-babe-42374d1b50d0"),
                "New Mark",
                TestSubjects.English,
                LocalDate.parse("2023-01-10"),
                1000,
                MarkValue(5450),
                SemesterId("e9b6a70e-87cd-464b-97a6-bd15c611bcdd"),
            )
        markService.addNewMark(mark)

        verify(emailSender).sendMail(eq("Neue Note"), anyString(), anyList())
        verify(emailSender).sendMail(anyString(), anyString(), eq(listOf(Berufsbildner.toSimpleUser())))
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"value\">${mark.value.value!!.format()}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(anyString(), contains("<span id=\"date\">${mark.date.format()}</span>"), anyList())
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"subject-name\">${mark.subject.name}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"weight\">${mark.weight.format()}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(anyString(), contains("<span id=\"mark-name\">${mark.name}</span>"), anyList())
    }

    @Test
    @WithMockLehrling
    fun new_mark_without_value_wont_send_notification() {
        val mark =
            Mark(
                MarkId("f67ce504-3436-4c4c-babe-42374d1b50d0"),
                "New Mark",
                TestSubjects.English,
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
        val oldMark = TestMarks.Companion.LernenderMarks.math1
        val newMark = TestMarks.Companion.SpecialMarks.EditedMathMark
        markService.editMark(newMark)

        verify(emailSender).sendMail(anyString(), anyString(), eq(listOf(Berufsbildner.toSimpleUser())))
        verify(emailSender).sendMail(eq("Note geändert"), anyString(), anyList())
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"old-date\">${oldMark.date.format()}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"old-subject-name\">${oldMark.subject.name}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"old-mark-name\">${oldMark.name}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"old-weight\">${oldMark.weight.format()}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"old-value\">${oldMark.value.value?.format()}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"new-date\">${newMark.date.format()}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"new-subject-name\">${newMark.subject.name}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"new-mark-name\">${newMark.name}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"new-weight\">${newMark.weight.format()}</span>"),
            anyList(),
        )
        verify(emailSender).sendMail(
            anyString(),
            contains("<span id=\"new-value\">${newMark.value.value?.format()}</span>"),
            anyList(),
        )
    }
}

private fun LocalDate.format() = format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

private fun Int.format() = BigDecimal(this.toDouble() / 1000).setScale(2, RoundingMode.DOWN).toPlainString()
