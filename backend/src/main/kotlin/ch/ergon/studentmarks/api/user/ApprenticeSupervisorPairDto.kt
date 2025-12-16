package ch.ergon.studentmarks.api.user

import java.util.UUID

data class ApprenticeSupervisorPairDto(
    val id: UUID,
    val apprentice: UserDto,
    val supervisor: UserDto,
    val emailNotification: Boolean,
)
