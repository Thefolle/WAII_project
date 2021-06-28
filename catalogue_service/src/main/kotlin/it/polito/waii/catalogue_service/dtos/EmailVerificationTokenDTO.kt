package it.polito.waii.catalogue_service.dtos

import java.time.LocalDateTime

class EmailVerificationTokenDTO(
    var token: String,
    var expiryDate: LocalDateTime,
    var username: String
)

