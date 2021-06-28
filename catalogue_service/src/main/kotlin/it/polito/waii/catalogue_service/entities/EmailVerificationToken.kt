package it.polito.waii.catalogue_service.entities


import it.polito.waii.catalogue_service.dtos.EmailVerificationTokenDTO
import javax.persistence.Entity;
import java.time.LocalDateTime
import java.util.*
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class EmailVerificationToken(
    @Id
    @GeneratedValue
    var EVTid: Long?,
    var token: String = UUID.randomUUID().toString(),
    var expiryDate: LocalDateTime,
    var username: String) {

    fun toDTO() = EmailVerificationTokenDTO(
        token = token,
        expiryDate = expiryDate,
        username = username
    )

}
