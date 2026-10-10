package ch.ergon.studentmarks.application.security

import ch.ergon.studentmarks.domain.model.user.User

/**
 * Who is performing the current call: a person behind a request, or a technical actor behind scheduled and
 * asynchronous work.
 */
sealed interface ActingUser {
    val name: String
}

data class ActingPerson(
    val user: User,
) : ActingUser {
    override val name: String get() = user.userName.value
}

sealed interface TechnicalActor : ActingUser {
    val permissions: Set<TechnicalPermission>
}

data object Job : TechnicalActor {
    override val name: String get() = "JOB"

    override val permissions: Set<TechnicalPermission> = TechnicalPermission.entries.toSet()
}

interface ActingUserProvider {
    val optionalActingUser: ActingUser?

    val actingPerson: ActingPerson
}
