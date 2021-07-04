package it.polito.waii.warehouse_service.services

import org.springframework.stereotype.Service

@Service
interface MailService {

    fun sendMessage(toMail: String, subject: String, mailBody: String)

}
