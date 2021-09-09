package it.polito.waii.catalogue_service.controllers


import it.polito.waii.catalogue_service.dtos.UpdateRoleDTO
import it.polito.waii.catalogue_service.dtos.UpdateUserDTO
import it.polito.waii.catalogue_service.dtos.UserDTO
import it.polito.waii.catalogue_service.entities.Action
import it.polito.waii.catalogue_service.services.UserServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
@RequestMapping("/user")
class UserController(private val userServiceImpl: UserServiceImpl) {

    @GetMapping("")
    fun retrieveInformation(): ResponseEntity<String> {
        val user = userServiceImpl.retrieveInformation()

        return ResponseEntity.status(HttpStatus.OK)
            .body(user.toJson())
    }

    @PatchMapping("/updateInfo")
    fun updateRole(@Validated @RequestBody updateUserDTO: UpdateUserDTO): ResponseEntity<String> {
        val user = userServiceImpl.updateUserInfo(updateUserDTO)

        return ResponseEntity.status(HttpStatus.OK)
            .body("User info updated successfully!\nNew information:\n" + user.toJson())
    }
}
