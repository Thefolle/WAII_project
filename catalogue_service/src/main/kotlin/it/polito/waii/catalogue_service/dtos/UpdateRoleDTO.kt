package it.polito.waii.catalogue_service.dtos

import it.polito.waii.catalogue_service.entities.Action
import it.polito.waii.catalogue_service.entities.Rolename

class UpdateRoleDTO (
    val username: String,
    val role: Rolename,
    val action: Action
)
