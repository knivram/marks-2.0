package ch.ergon.studentmarks.security

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.application.security.GrantedTo
import ch.ergon.studentmarks.application.security.NoPermissionRequired
import ch.ergon.studentmarks.application.security.TargetId
import ch.ergon.studentmarks.application.security.TargetUserResolver
import ch.ergon.studentmarks.infrastructure.security.GrantedToAuthorizationManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Authorization is declared by annotations carrying a [GrantedTo] declaration. These rules make sure every service
 * method declares one and that what a declaration needs is actually there.
 *
 * TODO: This test should not run as an integration test
 */
class AuthorizationAnnotationTest : IntegrationTest() {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var resolvers: List<TargetUserResolver<*>>

    private val serviceMethods: List<Method> by lazy {
        applicationContext
            .getBeansWithAnnotation(Service::class.java)
            .values
            .map { AopProxyUtils.ultimateTargetClass(it) }
            .filter { it.packageName.startsWith("ch.ergon.studentmarks.application") }
            .flatMap { serviceClass -> serviceClass.declaredMethods.filter { Modifier.isPublic(it.modifiers) && !it.isSynthetic } }
    }

    @Test
    fun services_are_found() {
        assertThat(serviceMethods.isEmpty(), `is`(false))
    }

    @Test
    fun no_two_resolvers_claim_the_same_id_type() {
        assertThat(resolvers.groupBy { it.idType }.filterValues { it.size > 1 }.keys, empty())
    }

    @TestFactory
    fun every_service_method_declares_its_authorization_exactly_once(): List<DynamicTest> =
        serviceMethods.map { method ->
            DynamicTest.dynamicTest(method.qualifiedName()) {
                val declarations =
                    method.annotations.filter {
                        it is NoPermissionRequired || GrantedToAuthorizationManager.grantedToOf(it.annotationClass.java) != null
                    }
                assertThat("${method.qualifiedName()} declarations", declarations.size, `is`(1))
            }
        }

    @TestFactory
    fun every_declaration_grants_someone(): List<DynamicTest> =
        serviceMethods.mapNotNull { method -> method.grantedTo()?.let { method to it } }.map { (method, grantedTo) ->
            DynamicTest.dynamicTest(method.qualifiedName()) {
                val grantsSomeone =
                    grantedTo.usersWithAnyOf.isNotEmpty() || grantedTo.technicalActorsWithAnyOf.isNotEmpty() || grantedTo.admins
                assertThat("${method.qualifiedName()} grants someone", grantsSomeone, `is`(true))
            }
        }

    @TestFactory
    fun user_permissions_decide_on_one_resolvable_target_id(): List<DynamicTest> =
        serviceMethods.filter { it.grantedTo()?.usersWithAnyOf?.isNotEmpty() == true }.map { method ->
            DynamicTest.dynamicTest(method.qualifiedName()) {
                val targets = method.parameters.filter { it.isAnnotationPresent(TargetId::class.java) }
                assertThat("${method.qualifiedName()} @TargetId parameters", targets.size, `is`(1))
                val resolvable = resolvers.any { it.idType.java == targets.single().type }
                assertThat("${method.qualifiedName()} has a resolver for ${targets.single().type.simpleName}", resolvable, `is`(true))
            }
        }

    @TestFactory
    fun a_target_id_is_only_declared_where_a_user_permission_decides_on_it(): List<DynamicTest> =
        serviceMethods.filter { method -> method.parameters.any { it.isAnnotationPresent(TargetId::class.java) } }.map { method ->
            DynamicTest.dynamicTest(method.qualifiedName()) {
                val decidedOn = method.grantedTo()?.usersWithAnyOf?.isNotEmpty() == true
                assertThat("${method.qualifiedName()} uses its @TargetId", decidedOn, `is`(true))
            }
        }

    private fun Method.grantedTo(): GrantedTo? = GrantedToAuthorizationManager.grantedToOf(this)

    private fun Method.qualifiedName() = "${declaringClass.simpleName}.$name"
}
