package it.polito.waii.catalogue_service.dtos

import javax.validation.constraints.Size

class UpdatePasswordDTO (
    @get :Size(min = 6, message = "The password length has to be greater or equal than 6.")
    val new_password: String,
    val new_password_confirm: String
)
