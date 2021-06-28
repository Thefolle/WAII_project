package it.polito.waii.catalogue_service.repositories


import it.polito.waii.catalogue_service.entities.EmailVerificationToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface EmailVerificationTokenRepository: CrudRepository<EmailVerificationToken, Long> {
    fun findByToken(token: String): Optional<EmailVerificationToken>
    fun deleteByExpiryDateLessThan(now: LocalDateTime): Unit
}
