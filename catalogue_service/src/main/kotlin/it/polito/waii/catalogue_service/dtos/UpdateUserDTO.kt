package it.polito.waii.catalogue_service.dtos

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class UpdateUserDTO (
    val username: String?,
    val email: String?,
    val name: String?,
    val surname: String?,
    val deliveryAddress: String?
)
