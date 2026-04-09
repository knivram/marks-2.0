package ch.ergon.studentmarks.infrastructure.security.authentication

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
class SecurityConfig(
    private val customAuthenticationFilter: CustomAuthenticationFilter,
    private val provider: CustomAuthenticationProvider,
) {
    @Bean
    @Throws(Exception::class)
    fun filterChain(
        http: HttpSecurity,
        userLogFilter: UserLogFilter,
    ): SecurityFilterChain? {
        http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(STATELESS) }
            .authenticationProvider(provider)
            .addFilterBefore(customAuthenticationFilter, BasicAuthenticationFilter::class.java)
            .addFilterAfter(userLogFilter, customAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager? =
        authenticationConfiguration.authenticationManager
}
