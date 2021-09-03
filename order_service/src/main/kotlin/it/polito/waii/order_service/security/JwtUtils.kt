package it.polito.waii.order_service.security

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import it.polito.waii.order_service.dtos.UserDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.security.Key
import java.security.SignatureException
import java.util.*


@Component
class JwtUtils {


    @Value("\${application.jwt.jwtExpirationMs}")
    private var jwtExpirationMs: Long = 1800000

    @Value("\${application.jwt.jwtSecret}")
    private var jwtSecret: String = "AFDRQMmyH6bxiFpTnkxDRqrZXQhVANRCvEIqFz7oJaJHvNkhD7yOLC93vOl4wne3"

    private val keyBytes = Decoders.BASE64.decode(jwtSecret)
    private val key: Key = Keys.hmacShaKeyFor(keyBytes)


    fun validateJwtToken(authToken: String): Boolean {

        try {
            Jwts.parserBuilder().setSigningKey(key).build().parse(authToken)
            return true
        } catch (e: SignatureException){
            throw e
        } catch (e: MalformedJwtException){
            throw e
        } catch (e: ExpiredJwtException){
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, e.message)
        } catch (e: UnsupportedJwtException){
            throw e
        } catch (e: IllegalArgumentException){
            throw e
        }

    }


    fun getDetailsFromJwtToken(authToken: String): UserDTO {

        val body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken).body
        val username = body["username"].toString()
        val name = body["name"].toString()
        val surname = body["surname"].toString()
        val deliveryAddress = body["deliveryAddress"].toString()
        val password = body["pass"].toString()
        val mail = body["mail"].toString()
        val isAdmin = body["isAdmin"]
        val isCustomer = body["isCustomer"]

        return  UserDTO(null,
                            uname = username,
                            pass = password,
                            name = name,
                            surname = surname,
                            mail = mail,
                            deliveryAddress = deliveryAddress,
                            isAdmin = isAdmin as Boolean,
                            isCustomer = isCustomer as Boolean,
                            isEn = true)
    }
}
