package ch.ergon.studentmarks.domain.user

data class User(
    val userId: UserId,
    val userName: UserName,
    val email: Email,
    val role: UserRole,
) {
    val isAdmin: Boolean = role == UserRole.ADMIN
    val isSupervisor: Boolean = role == UserRole.SUPERVISOR
}
