package ch.ergon.studentmarks.domain.email

import ch.ergon.studentmarks.domain.marks.Mark
import ch.ergon.studentmarks.domain.marks.MarkAverage
import ch.ergon.studentmarks.domain.user.SimpleUser
import ch.ergon.studentmarks.domain.user.User
import ch.ergon.studentmarks.domain.user.UserService
import ch.ergon.studentmarks.infrastructure.logging.Logger
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class EmailService(
    private val templateEngine: TemplateEngine,
    private val userService: UserService,
    private val emailSender: EmailSender,
) {
    companion object : Logger

    @Async
    fun sendAddMarkEmailHtml(
        apprentice: User,
        mark: Mark,
        subjectAverage: MarkAverage,
    ) {
        if (mark.value.value == null) return

        val supervisors: List<SimpleUser> = userService.getNotificationEmailListForApprentice(apprentice.userId)
        if (supervisors.isEmpty()) {
            return
        }

        val context = Context()
        context.setVariables(
            mapOf(
                "userName" to apprentice.userName.value,
                "date" to mark.date.format(),
                "subjectName" to mark.subject.name,
                "markName" to mark.name,
                "weight" to mark.weight.format(),
                "value" to mark.value.value.format(),
                "average" to subjectAverage.toFormattedString(),
            ),
        )
        val html = templateEngine.process("new-mark-added-template", context)

        emailSender.sendMail(
            subject = "Neue Note",
            html = html,
            receivers = supervisors,
        )
    }

    @Async
    fun sendEditMarkEmail(
        apprentice: User,
        newMark: Mark,
        oldMark: Mark,
        oldAverage: MarkAverage,
        newAverage: MarkAverage,
    ) {
        val supervisors: List<SimpleUser> = userService.getNotificationEmailListForApprentice(apprentice.userId)
        if (supervisors.isEmpty()) {
            return
        }

        val context = Context()
        context.setVariables(
            mapOf(
                "userName" to apprentice.userName.value,
                "oldAverage" to oldAverage.toFormattedString(),
                "newAverage" to newAverage.toFormattedString(),
                // old mark
                "oldDate" to oldMark.date.format(),
                "oldSubjectName" to oldMark.subject.name,
                "oldMarkName" to oldMark.name,
                "oldWeight" to oldMark.weight.format(),
                "oldValue" to (oldMark.value.value?.format() ?: "N/A"),
                // new mark
                "newDate" to newMark.date.format(),
                "newSubjectName" to newMark.subject.name,
                "newMarkName" to newMark.name,
                "newWeight" to newMark.weight.format(),
                "newValue" to (newMark.value.value?.format() ?: "N/A"),
            ),
        )
        val html = templateEngine.process("mark-updated-template", context)

        emailSender.sendMail(
            subject = "Note geändert",
            html = html,
            receivers = supervisors,
        )
    }
}

private fun MarkAverage.toFormattedString() = if (value == 0) "N/A" else value.format()

private fun LocalDate.format() = format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

private fun Int.format() = BigDecimal(this.toDouble() / 1000).setScale(2, RoundingMode.DOWN).toPlainString()
