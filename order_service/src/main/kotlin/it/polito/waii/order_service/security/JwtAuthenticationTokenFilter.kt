package it.polito.waii.order_service.security

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.util.StringUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Configuration
class JwtAuthenticationTokenFilter(val tokenProvider: JwtUtils): WebFilter {

    val HEADER_PREFIX: String = "Bearer "

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = resolveToken(exchange.request)

        if (StringUtils.hasText(token) && tokenProvider.validateJwtToken(token!!)) {
            val userDetailsDTO = tokenProvider.getDetailsFromJwtToken(token)
            val authentication : Authentication = UsernamePasswordAuthenticationToken(userDetailsDTO, null, userDetailsDTO.authorities)
            return chain.filter(exchange)
                .subscriberContext(ReactiveSecurityContextHolder.withAuthentication(authentication))
        }

        return chain.filter(exchange)
    }

    fun resolveToken(request: ServerHttpRequest): String? {
        val bearerToken: String = request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return null
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(HEADER_PREFIX)) {
            bearerToken.substring(7)
        } else null
    }
}