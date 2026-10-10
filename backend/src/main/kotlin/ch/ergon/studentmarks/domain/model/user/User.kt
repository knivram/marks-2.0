package ch.ergon.studentmarks.domain.model.user

import ch.ergon.studentmarks.domain.model.user.Role.ADMIN
import ch.ergon.studentmarks.domain.model.user.Role.SUPERVISOR
import java.util.UUID

data class User(
    val userId: UserId,
    val userName: UserName,
    val email: Email,
    val role: Role,
) {
    val isAdmin: Boolean = role == ADMIN
    val isSupervisor: Boolean = role == SUPERVISOR

    companion object {
        fun create(
            email: Email,
            userName: UserName,
            role: Role,
        ): User =
            User(
                userId = UserId(UUID.randomUUID()),
                email = email,
                userName = userName,
                role = role,
            )
    }
}
