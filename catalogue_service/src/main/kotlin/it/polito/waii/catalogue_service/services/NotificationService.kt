package it.polito.waii.catalogue_service.services

import it.polito.waii.catalogue_service.dtos.EmailVerificationTokenDTO

interface NotificationService {

    fun createToken(username: String): EmailVerificationTokenDTO

    fun getToken(token: String): EmailVerificationTokenDTO

    fun tokenNotExpired(token: String): Boolean

}
