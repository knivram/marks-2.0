package ch.ergon.studentmarks.api.user

import java.util.UUID

data class UserDto(
    val userId: UUID,
    val userName: String,
    val email: String,
    val role: UserRoleDto,
)
