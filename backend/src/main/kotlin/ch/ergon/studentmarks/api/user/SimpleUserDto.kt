package ch.ergon.studentmarks.api.user

data class SimpleUserDto(
    val email: String,
    val userName: String,
    val role: UserRoleDto,
)
