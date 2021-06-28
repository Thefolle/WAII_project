package it.polito.waii.catalogue_service.controllers

import it.polito.waii.catalogue_service.dtos.LoginDTO
import it.polito.waii.catalogue_service.dtos.RegisterDTO
import it.polito.waii.catalogue_service.dtos.UserDTO
import it.polito.waii.catalogue_service.services.UserServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthenticationController(private val userServiceImpl: UserServiceImpl){

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerDTO: RegisterDTO): ResponseEntity<String>{

        userServiceImpl.addUser(registerDTO)

        return ResponseEntity.status(HttpStatus.CREATED)
            .body("User registered successfully." +
                    " You will receive an email shortly to confirm your registration.")
    }

    @GetMapping("/registrationConfirm")
    fun registrationConfirm(@RequestParam("token") token: String): ResponseEntity<UserDTO>{

        val user = userServiceImpl.registrationConfirm(token)

        return ResponseEntity.status(HttpStatus.OK).body(user)
    }

    @PostMapping("/login")
    fun login(@RequestBody loginDTO: LoginDTO, response: HttpServletResponse): ResponseEntity<Nothing> {
        userServiceImpl.authenticateUser(loginDTO, response)

        return ResponseEntity.status(HttpStatus.CREATED).build()
    }


}
