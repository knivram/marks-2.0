package ch.ergon.studentmarks.domain.model.user

import java.util.UUID

data class User(
    val userId: UserId,
    val userName: UserName,
    val email: Email,
    val role: UserRole,
) {
    val isAdmin: Boolean = role == UserRole.ADMIN
    val isSupervisor: Boolean = role == UserRole.SUPERVISOR

    companion object {
        fun create(email: Email, userName: UserName, role: UserRole): User =
            User(
                userId = UserId(UUID.randomUUID()),
                email = Email(email.value.lowercase()),
                userName = UserName(userName.value.lowercase()),
                role = role,
            )
    }
}
