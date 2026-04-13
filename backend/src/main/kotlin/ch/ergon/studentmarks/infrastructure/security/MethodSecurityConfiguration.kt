package ch.ergon.studentmarks.infrastructure.security

import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.domain.repository.UserRepository
import ch.ergon.studentmarks.infrastructure.security.authorization.CustomPermissionEvaluator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

@Configuration
@EnableMethodSecurity()
class MethodSecurityConfiguration(
    val markRepository: MarkRepository,
    val userRepository: UserRepository,
) {
    @Bean
    fun methodSecurityExpressionHandler(): MethodSecurityExpressionHandler {
        val expressionHandler = DefaultMethodSecurityExpressionHandler()
        expressionHandler.setPermissionEvaluator(
            CustomPermissionEvaluator(
                markRepository,
                userRepository,
            ),
        )
        return expressionHandler
    }
}
