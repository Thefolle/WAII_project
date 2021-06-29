package it.polito.waii.catalogue_service.services

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MailServiceImpl(val mailSender: JavaMailSender): MailService {

    override fun sendMessage(toMail: String, subject: String, mailBody: String) {
        val message = SimpleMailMessage()
        message.setTo(toMail)
        message.setSubject(subject)
        message.setText(mailBody)

        mailSender.send(message)
    }

}
