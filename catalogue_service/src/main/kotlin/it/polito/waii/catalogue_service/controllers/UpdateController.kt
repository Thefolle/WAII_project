package it.polito.waii.catalogue_service.controllers

import it.polito.waii.catalogue_service.dtos.UpdatePasswordDTO
import it.polito.waii.catalogue_service.dtos.UpdateRoleDTO
import it.polito.waii.catalogue_service.entities.Action
import it.polito.waii.catalogue_service.services.UserServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
@RequestMapping("/update")
class UpdateController(private val userServiceImpl: UserServiceImpl) {

    @PostMapping("/password")
    fun updatePassword(@Valid @RequestBody updatePasswordDTO: UpdatePasswordDTO): ResponseEntity<String> {
        userServiceImpl.updatePassword(updatePasswordDTO)

        return ResponseEntity.status(HttpStatus.OK)
            .body("User password updated successfully!")
    }

    @PostMapping("/role")
    fun updateRole(@Valid @RequestBody updateRoleDTO: UpdateRoleDTO): ResponseEntity<String> {
        if (updateRoleDTO.action == Action.ADD)
            userServiceImpl.addRole(updateRoleDTO.role, updateRoleDTO.username)
        else if (updateRoleDTO.action == Action.REMOVE)
            userServiceImpl.removeRole(updateRoleDTO.role, updateRoleDTO.username)
        else
            throw  ResponseStatusException(HttpStatus.BAD_REQUEST, "UNKNOWN ROLE SPECIFIED")

        return ResponseEntity.status(HttpStatus.OK)
            .body("User role updated successfully!")
    }
}
