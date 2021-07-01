package it.polito.waii.catalogue_service.dtos

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class UpdateUserDTO (
    @get: Size(min = 5, max = 25)
    val username: String,
    @get: Pattern(regexp = ".*@.*", message = "Invalid email.")
    val email: String,
    @get :NotBlank(message = "The name cannot be empty.")
    val name: String,
    @get :NotBlank(message = "The surname cannot be empty.")
    val surname: String,
    val deliveryAddress: String
)
