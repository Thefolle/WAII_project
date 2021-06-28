package it.polito.waii.catalogue_service.services


import it.polito.waii.catalogue_service.dtos.LoginDTO
import it.polito.waii.catalogue_service.dtos.RegisterDTO
import it.polito.waii.catalogue_service.dtos.UserDTO
import it.polito.waii.catalogue_service.entities.Rolename
import org.springframework.security.core.userdetails.UserDetailsService
import javax.servlet.http.HttpServletResponse

interface UserService: UserDetailsService {

//    fun addUser(customer: Customer, userDTO: UserDetailsDTO)

    fun authenticateUser(loginDTO: LoginDTO, response: HttpServletResponse)

    fun addUser(registerDTO: RegisterDTO)

    fun addRole(role: Rolename, username: String)

    fun removeRole(role: Rolename, username: String)

    fun enable(username: String)

    fun disable(username: String)

    override fun loadUserByUsername(username: String) : UserDTO

    fun registrationConfirm(token: String): UserDTO
}
