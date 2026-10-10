package ch.ergon.studentmarks.infrastructure.security.authentication

import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
class SecurityConfig(
    private val subjectHeaderAuthenticationFilter: SubjectHeaderAuthenticationFilter,
    private val userLogFilter: UserLogFilter,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(STATELESS) }
            .authorizeHttpRequests {
                // Spring Security authorizes the ERROR dispatch too, but the subject header filter does not run on it, so the
                // forward to /error would be rejected and every unhandled exception would surface as a bodiless 403.
                // TODO: Consider an ExceptionControllerAdvice that turns every exception into a response instead, so no
                //  request ever reaches the error dispatch.
                it.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                it.anyRequest().authenticated()
            }.addFilterBefore(subjectHeaderAuthenticationFilter, BasicAuthenticationFilter::class.java)
            .addFilterAfter(userLogFilter, SubjectHeaderAuthenticationFilter::class.java)
        return http.build()
    }
}
