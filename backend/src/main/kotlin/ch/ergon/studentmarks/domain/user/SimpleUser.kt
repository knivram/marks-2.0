package ch.ergon.studentmarks.domain.user

data class SimpleUser(
    val email: Email,
    val userName: UserName,
    val role: UserRole,
)
