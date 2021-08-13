package it.polito.waii.warehouse_service.services

interface MailService {

    fun sendMessage(toMail: String, subject: String, mailBody: String)

}
