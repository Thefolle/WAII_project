package it.polito.waii.catalogue_service.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtAuthenticationTokenFilter(val jwtUtils: JwtUtils): OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token: String
        val header = request.getHeader("Authorization")
        if ( header != null && header.startsWith("Bearer ")) {
            println("Sono nel filter JWT")
            token = header.substring(7)
            if (jwtUtils.validateJwtToken(token)) {
                val userDetailsDTO = jwtUtils.getDetailsFromJwtToken(token)
                println(userDetailsDTO.toJson())
                println(userDetailsDTO.authorities)
                val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(userDetailsDTO, null, userDetailsDTO.authorities)
                usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                val authentication : Authentication = usernamePasswordAuthenticationToken

                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)

    }

}
