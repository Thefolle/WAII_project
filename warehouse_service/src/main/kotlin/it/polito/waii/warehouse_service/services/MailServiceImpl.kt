package it.polito.waii.warehouse_service.services

import org.springframework.http.HttpStatus
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class MailServiceImpl(val mailSender: JavaMailSender): MailService {

    override fun sendMessage(toMail: String, subject: String, mailBody: String) {
        val message = SimpleMailMessage()
        message.setTo(toMail)
        message.setSubject(subject)
        message.setText(mailBody)

        try {
            mailSender.send(message)
        } catch (exception: MailException) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The mail service cannot be reached due to the following reason: ${exception.message}")
        }

    }

}
