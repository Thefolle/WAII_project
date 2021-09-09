package it.polito.waii.catalogue_service.services


import it.polito.waii.catalogue_service.dtos.*
import it.polito.waii.catalogue_service.entities.Rolename
import it.polito.waii.catalogue_service.entities.User
import it.polito.waii.catalogue_service.repositories.UserRepository
import it.polito.waii.catalogue_service.security.JwtUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletResponse

@Service
@Transactional
class UserServiceImpl(val userRepository: UserRepository,
                             val passwordEncoder: PasswordEncoder,
                             val notificationService: NotificationServiceImpl,
                             val mailService: MailServiceImpl,
                             val jwtUtils: JwtUtils
): UserService {

    @Value("\${application.jwt.jwtHeader}")
    private lateinit var jwtHeader: String


    private fun getUserByUsername(username: String): User {
        val userOptional = userRepository.findByUsername(username)
        if (userOptional.isEmpty) throw ResponseStatusException(HttpStatus.NOT_FOUND,
            "No user with username $username exists.")
        return userOptional.get()
    }

    override fun loadUserByUsername(username: String): UserDTO {
        val user = getUserByUsername(username)
        return user.toDTO()
    }

    override fun authenticateUser(loginDTO: LoginDTO, response: HttpServletResponse) {
        val user = loadUserByUsername(loginDTO.username)

        if (!user.isEn) throw ResponseStatusException(HttpStatus.FORBIDDEN, "This user is not enabled!")
        if (!passwordEncoder.matches(loginDTO.password, user.pass)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid password.")
        }

        val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(user, null, user.authorities)
        val authentication: Authentication = usernamePasswordAuthenticationToken
        val token = jwtUtils.generateJwtToken(authentication)
        response.setHeader(jwtHeader, token)
    }

    override fun addUser(registerDTO: RegisterDTO) {
        if (userRepository.existsByUsername(registerDTO.username)) {
            throw ResponseStatusException(HttpStatus.IM_USED, "The username is already in use.")
        } else if (userRepository.existsByEmail(registerDTO.email)) {
            throw ResponseStatusException(HttpStatus.IM_USED, "The email is already linked to another account.")
        } else if (registerDTO.password != registerDTO.confirmPassword) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Password and confirmPassword don't match.")
        }

        val user = User(
            uid = null,
            username = registerDTO.username,
            password = passwordEncoder.encode(registerDTO.password),
            name = registerDTO.name,
            surname = registerDTO.surname,
            email = registerDTO.email,
            deliveryAddress = registerDTO.deliveryAddress,
            isEnabled = false,
            isAdmin = false,
            isCustomer = true
        )

        userRepository.save(user)

        val tokenDTO = notificationService.createToken(user.username)
        mailService.sendMessage(
            toMail = user.email, subject = "Group19 Final Project - Registration Confirmation",
            mailBody = "Confirm your registration by clicking on the following link: localhost:8080/catalogue/auth/registrationConfirm?token=" + tokenDTO.token
        )
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun addRole(role: Rolename, username: String) {
        val user = getUserByUsername(username)
        user.addRole(role)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun removeRole(role: Rolename, username: String) {
        val user = getUserByUsername(username)
        user.removeRole(role)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun enable(username: String) {
        val user = getUserByUsername(username)
        user.isEnabled = true
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun disable(username: String) {
        val user = getUserByUsername(username)
        user.isEnabled = false
    }

    override fun registrationConfirm(token: String): UserDTO {
        val verifyToken = notificationService.getToken(token)
        if (!notificationService.tokenNotExpired(verifyToken.token)) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "The verification email has expired!"
        )
        enable(verifyToken.username)

        return loadUserByUsername(verifyToken.username)
    }

    override fun updatePassword(update: UpdatePasswordDTO) {
        if (update.newPassword != update.newPasswordConfirm) throw ResponseStatusException(
            HttpStatus.CONFLICT,
            "Password and confirmPassword don't match!"
        )
        val principal = SecurityContextHolder.getContext().authentication.principal
        val username = if (principal is UserDTO) {
            principal.username
        } else {
            principal.toString()
        }

        val user = getUserByUsername(username)
        user.password = passwordEncoder.encode(update.newPassword)
    }

    override fun retrieveInformation(): UserDTO {
        val principal = SecurityContextHolder.getContext().authentication.principal
        val username = if (principal is UserDTO) {
            principal.username
        } else {
            principal.toString()
        }

        return loadUserByUsername(username)
    }

    override fun updateUserInfo(update: UpdateUserDTO): UserDTO {
        if (update.username != null && userRepository.existsByUsername(update.username)) {
            throw ResponseStatusException(HttpStatus.IM_USED, "The username is already in use.")
        } else if (update.email != null && userRepository.existsByEmail(update.email)) {
            throw ResponseStatusException(HttpStatus.IM_USED, "The email is already linked to another account.")
        }

        val principal = SecurityContextHolder.getContext().authentication.principal
        val current_username = if (principal is UserDTO) {
            principal.username
        } else {
            principal.toString()
        }

        val user = getUserByUsername(current_username)
        user.username = update.username ?: user.username
        user.email = update.email ?: user.email
        user.name = update.name ?: user.name
        user.surname = update.surname ?: user.surname
        user.deliveryAddress = update.deliveryAddress ?: user.deliveryAddress

        return user.toDTO()
    }

    override fun getAllAdminEmails(): Set<String> {
        return userRepository
            .findAll()
            .filter {
                it
                    .isAdmin
            }
            .map {
                it
                    .email
            }
            .toSet()
    }

    override fun getUserEmail(username: String): String {
        val eventualUser = userRepository.findByUsername(username)
        if (eventualUser.isEmpty) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No user with username $username exists.")
        } else {
            return eventualUser.get().email
        }
    }
}
