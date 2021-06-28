package it.polito.waii.catalogue_service.services

import it.polito.waii.catalogue_service.dtos.EmailVerificationTokenDTO
import it.polito.waii.catalogue_service.entities.EmailVerificationToken
import it.polito.waii.catalogue_service.repositories.EmailVerificationTokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
@Transactional
class NotificationServiceImpl(
    val emailVerificationTokenRepository: EmailVerificationTokenRepository
): NotificationService {


    @Value("\${application.jwt.jwtExpirationMs}")
    private lateinit var jwtExpirationMs: String

    override fun createToken(username: String): EmailVerificationTokenDTO {
        val token = EmailVerificationToken(
            null,
            expiryDate = LocalDateTime.now().plusSeconds(jwtExpirationMs.toLong() / 1000L),
            username = username
        )
        emailVerificationTokenRepository.save(token)
        return token.toDTO()
    }

    override fun getToken(token: String): EmailVerificationTokenDTO {
        val optionalToken = emailVerificationTokenRepository.findByToken(token)
        if (optionalToken.isEmpty) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found!")
        return optionalToken.get().toDTO()
    }

    override fun tokenNotExpired(token: String): Boolean{
        val t = getToken(token)
        return !t.expiryDate.isBefore(LocalDateTime.now())
    }

    // clear Expired Tokens once a day
    @Scheduled(fixedRate = 86400000)
    fun clearExpiredTokens(){
        val now = LocalDateTime.now()
        emailVerificationTokenRepository.deleteByExpiryDateLessThan(now)
    }
}
