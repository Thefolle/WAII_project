package it.polito.waii.catalogue_service.controllers


import it.polito.waii.catalogue_service.dtos.*
import it.polito.waii.catalogue_service.entities.Action
import it.polito.waii.catalogue_service.services.UserServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
@RequestMapping("/user")
class UserController(private val userServiceImpl: UserServiceImpl) {

    @GetMapping("/info")
    fun retrieveInformation(): ResponseEntity<ReturnUserInfoDto> {
        val user = userServiceImpl.retrieveInformation()

        return ResponseEntity.status(HttpStatus.OK)
            .body(user.toReturnUserInfoDto())
    }

    @PatchMapping("/info")
    fun updateInformation(@Validated @RequestBody updateUserDTO: UpdateUserDTO): ResponseEntity<ReturnUserInfoDto> {
        val user = userServiceImpl.updateUserInfo(updateUserDTO)

        return ResponseEntity.status(HttpStatus.OK)
            .body(user.toReturnUserInfoDto())
    }

    @PatchMapping("/password")
    fun updatePassword(@Valid @RequestBody updatePasswordDTO: UpdatePasswordDTO): ResponseEntity<String> {
        userServiceImpl.updatePassword(updatePasswordDTO)

        return ResponseEntity.status(HttpStatus.OK)
            .body("User password updated successfully!")
    }

    @PatchMapping("/role")
    fun updateRole(@Valid @RequestBody updateRoleDTO: UpdateRoleDTO): ResponseEntity<String> {
        if (updateRoleDTO.action == Action.ADD)
            userServiceImpl.addRole(updateRoleDTO.role, updateRoleDTO.username)
        else if (updateRoleDTO.action == Action.REMOVE)
            userServiceImpl.removeRole(updateRoleDTO.role, updateRoleDTO.username)
        else
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Please, specify an admissible role.")

        return ResponseEntity.status(HttpStatus.OK)
            .body("User role updated successfully!")
    }
}
