package it.polito.waii.catalogue_service

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.PropertySource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@SpringBootApplication
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
}

fun main(args: Array<String>) {
    runApplication<CatalogueServiceApplication>(*args)
}
