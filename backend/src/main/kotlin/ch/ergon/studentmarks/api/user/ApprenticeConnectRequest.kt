package ch.ergon.studentmarks.api.user

import ch.ergon.studentmarks.domain.user.Email

data class ApprenticeConnectRequest(
    val apprenticeEmail: Email,
    val supervisorEmail: Email,
)
