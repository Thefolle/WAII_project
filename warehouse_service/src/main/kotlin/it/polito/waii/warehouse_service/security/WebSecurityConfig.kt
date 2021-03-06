package it.polito.waii.warehouse_service.security


import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class WebSecurityConfig (val authenticationEntryPoint: AuthenticationEntryPoint, val jwtUtils: JwtUtils): WebSecurityConfigurerAdapter(){

    override fun configure(authenticationManagerBuilder: AuthenticationManagerBuilder) {

        /*authenticationManagerBuilder
            .userDetailsService(userService)
            .passwordEncoder(passwordEncoder)*/

        /*authenticationManagerBuilder.inMemoryAuthentication()
                .withUser("root")
                .password(passwordEncoder.encode("admin"))
                .roles("user")*/
    }

    override fun configure(http: HttpSecurity) {

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http.authorizeRequests().antMatchers("/**").authenticated()

        //http.formLogin().permitAll().and().logout().permitAll()

        http.csrf().disable()

        http.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)

        http.addFilterBefore(JwtAuthenticationTokenFilter(jwtUtils), UsernamePasswordAuthenticationFilter::class.java)
    }

}
