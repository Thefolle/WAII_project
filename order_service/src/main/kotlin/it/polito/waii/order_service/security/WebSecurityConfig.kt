package it.polito.waii.order_service.security

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository


@EnableWebFluxSecurity
class WebSecurityConfig(val jwtAuthenticationTokenFilter: JwtAuthenticationTokenFilter) {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {

        return http
            .csrf().disable()
            .httpBasic().disable()
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // stateless session management
            .authorizeExchange { it.anyExchange().permitAll() }
            .addFilterAt(jwtAuthenticationTokenFilter, SecurityWebFiltersOrder.HTTP_BASIC)
            .build()

    }
}