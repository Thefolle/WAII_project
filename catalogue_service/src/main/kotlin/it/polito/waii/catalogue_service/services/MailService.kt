package it.polito.waii.catalogue_service.services

interface MailService {

    fun sendMessage(toMail: String, subject: String, mailBody: String)

}
