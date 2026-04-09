package ch.ergon.studentmarks.domain.email

import ch.ergon.studentmarks.domain.user.SimpleUser
import ch.ergon.studentmarks.infrastructure.logging.Logger
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailSender(
    val mailSender: JavaMailSender,
    @param:Value("\${spring.mail.username}")
    private val fromEmail: String,
    @param:Value("\${marks.mail.enabled:true}")
    private val emailIsEnabled: Boolean,
) {
    companion object : Logger

    fun sendMail(
        subject: String,
        html: String,
        receivers: List<SimpleUser>,
    ) {
        if (!emailIsEnabled) return

        LOG.info("Sending Email from $fromEmail to $receivers")
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")
        helper.setSubject(subject)
        helper.setFrom(fromEmail)
        helper.setTo(receivers.map { it.email.value }.toTypedArray())

        // Add HTML email body
        val messageBodyPart = MimeBodyPart()
        messageBodyPart.setContent(html, "text/html; charset=UTF-8")
        val mimeMultipart = MimeMultipart("related")
        mimeMultipart.addBodyPart(messageBodyPart)
        message.setContent(mimeMultipart)

        mailSender.send(message)
    }
}
