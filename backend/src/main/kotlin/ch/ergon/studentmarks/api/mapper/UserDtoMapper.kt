package ch.ergon.studentmarks.api.mapper

import ch.ergon.studentmarks.api.generated.dto.ApprenticeSupervisorPairDto
import ch.ergon.studentmarks.api.generated.dto.UserDto
import ch.ergon.studentmarks.api.generated.dto.UserRoleDto
import ch.ergon.studentmarks.application.response.ApprenticeSupervisorPairWithUsers
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPairId
import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.Role
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.model.user.UserName

// TODO Consider konvert
fun Role.toDto(): UserRoleDto =
    when (this) {
        Role.ADMIN -> UserRoleDto.ADMIN
        Role.SUPERVISOR -> UserRoleDto.SUPERVISOR
        Role.APPRENTICE -> UserRoleDto.APPRENTICE
    }

fun UserRoleDto.toDomain(): Role =
    when (this) {
        UserRoleDto.ADMIN -> Role.ADMIN
        UserRoleDto.SUPERVISOR -> Role.SUPERVISOR
        UserRoleDto.APPRENTICE -> Role.APPRENTICE
    }

fun User.toDto(): UserDto =
    UserDto(
        userId = userId.value,
        userName = userName.value,
        email = email.value,
        role = role.toDto(),
    )

fun UserDto.toDomain(): User =
    User(
        userId = UserId(userId),
        userName = UserName(userName),
        email = Email.create(email),
        role = role.toDomain(),
    )

fun ApprenticeSupervisorPairWithUsers.toDto(): ApprenticeSupervisorPairDto =
    ApprenticeSupervisorPairDto(
        id = pair.id.value,
        apprentice = apprentice.toDto(),
        supervisor = supervisor.toDto(),
        emailNotification = pair.emailNotification,
    )

fun ApprenticeSupervisorPairDto.toDomain(): ApprenticeSupervisorPair =
    ApprenticeSupervisorPair(
        id = ApprenticeSupervisorPairId(id),
        apprenticeId = UserId(apprentice.userId),
        supervisorId = UserId(supervisor.userId),
        emailNotification = emailNotification,
    )
