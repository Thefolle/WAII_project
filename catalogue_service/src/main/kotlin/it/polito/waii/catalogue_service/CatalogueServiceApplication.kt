package it.polito.waii.catalogue_service

import it.polito.waii.catalogue_service.entities.User
import it.polito.waii.catalogue_service.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import java.time.LocalDateTime
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@SpringBootApplication
@EnableEurekaClient
//@PropertySource(value = ["classpath:application.properties"], ignoreResourceNotFound = false)
class CatalogueServiceApplication{

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun getMailSender(@Value("\${spring.mail.host}") host: String,
                      @Value("\${spring.mail.port}") port: Int,
                      @Value("\${spring.mail.username}") username: String,
                      @Value("\${spring.mail.password}") password: String,
                      @Value("\${spring.mail.protocol}") protocol: String,
                      @Value("\${spring.mail.properties.mail.smtp.auth}") auth: Boolean,
                      @Value("\${spring.mail.properties.mail.smtp.starttls.enable}") enable: Boolean,
                      @Value("\${spring.mail.properties.mail.debug}") debug: Boolean
    ): JavaMailSender {

        val javaMailSenderImpl = JavaMailSenderImpl()
        javaMailSenderImpl.host = host
        javaMailSenderImpl.port = port
        javaMailSenderImpl.username = username
        javaMailSenderImpl.password = password
        val props: Properties = javaMailSenderImpl.javaMailProperties
        props["mail.transport.protocol"] = protocol
        props["mail.smtp.auth"] = auth
        props["mail.smtp.starttls.enable"] = enable
        props["mail.debug"] = debug

        // Uncomment to test the mail connection at startup
        //javaMailSenderImpl.testConnection()

        return javaMailSenderImpl
    }

    @Bean
    fun getAuthenticationEntryPoint(): AuthenticationEntryPoint {
        return AuthenticationEntryPoint() { httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, authenticationException: AuthenticationException ->
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        }
    }

    @Bean
    fun populateDB (userRepository: UserRepository): ApplicationRunner {
        return ApplicationRunner {

            val password = passwordEncoder().encode("123456")

            // Create new users
            val u1 = User(
                null, "Sofia", password,
                "Sofiya", "Reyes",
                "sofiya@yopmail.com", "via Garibaldi 9",
                true, true, true
            )
            userRepository.save(u1)

            val u2 = User(
                null, "Lucas", password,
                "Luca", "Bernardi",
                "lucas@yopmail.com", "via Veneto 19",
                true, false, true
            )
            userRepository.save(u2)

            val u3 = User(
                null, "Josef", password,
                "Josef", "Baldi",
                "baldi@yopmail.com", "corso Robbia 37",
                true, false, true
            )
            userRepository.save(u3)

            val u4 = User(
                null, "Martina", password,
                "Martina", "Cobella",
                "marta@yopmail.com", "via Urbino 28",
                true, false, true
            )
            userRepository.save(u4)

            val u5 = User(
                null, "Lusy", password,
                "Lusy", "Cesare",
                "lusy@yopmail.com", "corso Gambasca 42",
                true, false, true
            )
            userRepository.save(u5)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<CatalogueServiceApplication>(*args)
}
