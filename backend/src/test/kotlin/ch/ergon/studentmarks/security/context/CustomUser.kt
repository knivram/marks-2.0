package ch.ergon.studentmarks.security.context

import org.springframework.security.test.context.support.WithSecurityContext

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
annotation class WithMockCustomUser(
    val email: String,
)

@Retention(AnnotationRetention.RUNTIME)
@WithMockCustomUser("bernd.lienberger@ergon.ch")
annotation class WithMockAdmin

@Retention(AnnotationRetention.RUNTIME)
@WithMockCustomUser("marvin.kramer@ergon.ch")
annotation class WithMockLehrling

@Retention(AnnotationRetention.RUNTIME)
@WithMockCustomUser("thomas.boksberger@ergon.ch")
annotation class WithMockBerufsbildner
