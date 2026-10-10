package ch.ergon.studentmarks.application.security

import ch.ergon.studentmarks.domain.model.user.UserPermission

/**
 * Who may call a method secured by an annotation carrying this declaration. The clauses are alternatives:
 *
 *  - an [ActingPerson] whose role holds one of [usersWithAnyOf] and who can reach the owner of the [TargetId]
 *    argument (admins reach everyone, supervisors themselves and their apprentices, apprentices themselves)
 *  - a [TechnicalActor] holding one of [technicalActorsWithAnyOf]
 *  - an admin, if [admins] is set
 *
 * Enforced by `GrantedToAuthorizationManager`, which intercepts every method secured this way.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class GrantedTo(
    val usersWithAnyOf: Array<UserPermission> = [],
    val technicalActorsWithAnyOf: Array<TechnicalPermission> = [],
    val admins: Boolean = false,
)

/**
 * Marks the argument whose owning user a [GrantedTo.usersWithAnyOf] clause checks access to.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class TargetId

/**
 * Any authenticated caller may invoke the method, with any argument.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NoPermissionRequired(
    @Suppress("Unused")
    val reason: String,
)

enum class TechnicalPermission {
    MARK_PURGE,
    NOTIFICATION_SEND,
}
