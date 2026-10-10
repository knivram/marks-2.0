package ch.ergon.studentmarks.infrastructure.security

import ch.ergon.studentmarks.application.security.ActingPerson
import ch.ergon.studentmarks.application.security.ActingUser
import ch.ergon.studentmarks.application.security.ActingUserProvider
import ch.ergon.studentmarks.application.security.GrantedTo
import ch.ergon.studentmarks.application.security.TargetId
import ch.ergon.studentmarks.application.security.TargetUserResolver
import ch.ergon.studentmarks.application.security.TechnicalActor
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.model.user.UserPermission
import ch.ergon.studentmarks.domain.repository.ApprenticeSupervisorRepository
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.support.AopUtils
import org.springframework.aop.support.StaticMethodMatcherPointcut
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.authorization.method.AuthorizationInterceptorsOrder
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor
import org.springframework.security.core.Authentication
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.util.function.Supplier
import kotlin.reflect.KClass

/**
 * Enforces [GrantedTo]: intercepts every method whose annotation carries a declaration and grants the call if any of
 * its clauses holds. A user clause checks the role's permission first, so the [TargetId] is only resolved for a caller
 * who may perform the action at all. An unknown target is not found, a known one out of reach is forbidden.
 */
class GrantedToAuthorizationManager(
    private val actingUserProvider: ObjectProvider<ActingUserProvider>,
    private val apprenticeSupervisorRepository: ObjectProvider<ApprenticeSupervisorRepository>,
    resolvers: ObjectProvider<TargetUserResolver<*>>,
) : AuthorizationManager<MethodInvocation> {
    private val resolversByIdType: Map<KClass<*>, TargetUserResolver<*>> by lazy {
        resolvers.stream().toList().associateBy { it.idType }
    }

    override fun authorize(
        authentication: Supplier<Authentication>,
        invocation: MethodInvocation,
    ): AuthorizationDecision {
        // The invocation may hand us the interface method (e.g. a generated controller API); the annotations and the
        // @TargetId parameter live on the concrete override.
        val method = AopUtils.getMostSpecificMethod(invocation.method, invocation.`this`?.javaClass)
        val grantedTo = grantedToOf(method) ?: return AuthorizationDecision(false)
        val actor = actingUserProvider.getObject().optionalActingUser ?: return AuthorizationDecision(false)

        return AuthorizationDecision(isGranted(actor, grantedTo, targetIdOf(method, invocation.arguments)))
    }

    // Spring Security 6 still declares the deprecated check() abstract; the interceptor calls authorize().
    @Deprecated("Implemented only to satisfy the interface; authorize() decides.")
    override fun check(
        authentication: Supplier<Authentication>,
        invocation: MethodInvocation,
    ): AuthorizationDecision = authorize(authentication, invocation)

    private fun isGranted(
        actor: ActingUser,
        grantedTo: GrantedTo,
        targetId: Any?,
    ): Boolean =
        when (actor) {
            is TechnicalActor -> grantedTo.technicalActorsWithAnyOf.any { it in actor.permissions }
            is ActingPerson -> (grantedTo.admins && actor.user.isAdmin) || isGrantedToUser(actor.user, grantedTo.usersWithAnyOf, targetId)
        }

    private fun isGrantedToUser(
        user: User,
        permissions: Array<UserPermission>,
        targetId: Any?,
    ): Boolean {
        if (permissions.none { it in user.role.permissions }) return false
        val targetId = targetId ?: return false
        return canReach(user, resolversByIdType.getValue(targetId::class).resolve(targetId))
    }

    private fun canReach(
        user: User,
        ownerId: UserId,
    ): Boolean =
        user.isAdmin || ownerId == user.userId ||
            (user.isSupervisor && apprenticeSupervisorRepository.getObject().isConnected(ownerId, user.userId))

    private fun targetIdOf(
        method: Method,
        arguments: Array<Any?>,
    ): Any? =
        method.parameters
            .indexOfFirst { it.isAnnotationPresent(TargetId::class.java) }
            .takeIf { it >= 0 }
            ?.let { arguments[it] }

    companion object {
        /** The [GrantedTo] declaration an element carries through one of its annotations, if any. */
        fun grantedToOf(element: AnnotatedElement): GrantedTo? =
            MergedAnnotations
                .from(element, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(GrantedTo::class.java)
                .takeIf { it.isPresent }
                ?.synthesize()
    }
}

/**
 * Registers the [GrantedToAuthorizationManager] the way `@PreAuthorize` is registered: as an
 * [AuthorizationManagerBeforeMethodInterceptor] that the auto-proxy creator applies to every matching bean method.
 */
@Configuration
class GrantedToAuthorizationConfiguration {
    companion object {
        // Static so that the advisor exists before any bean it may have to proxy.
        @JvmStatic
        @Bean
        // Puts the advice where `@PreAuthorize` would run, i.e. before the transaction interceptor.
        @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
        fun grantedToAuthorizationInterceptor(
            actingUserProvider: ObjectProvider<ActingUserProvider>,
            apprenticeSupervisorRepository: ObjectProvider<ApprenticeSupervisorRepository>,
            resolvers: ObjectProvider<TargetUserResolver<*>>,
        ): AuthorizationManagerBeforeMethodInterceptor =
            AuthorizationManagerBeforeMethodInterceptor(
                SecuredMethodPointcut,
                GrantedToAuthorizationManager(actingUserProvider, apprenticeSupervisorRepository, resolvers),
            ).apply { order = AuthorizationInterceptorsOrder.PRE_AUTHORIZE.order }
    }
}

/**
 * Methods carrying an annotation with a [GrantedTo] declaration. Beans with at least one match are proxied.
 */
private object SecuredMethodPointcut : StaticMethodMatcherPointcut() {
    override fun matches(
        method: Method,
        targetClass: Class<*>,
    ): Boolean = GrantedToAuthorizationManager.grantedToOf(AopUtils.getMostSpecificMethod(method, targetClass)) != null
}
