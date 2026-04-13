package ch.ergon.studentmarks.api.mapper

import ch.ergon.studentmarks.api.generated.dto.ApprenticeSupervisorPairDto
import ch.ergon.studentmarks.api.generated.dto.SimpleUserDto
import ch.ergon.studentmarks.api.generated.dto.UserDto
import ch.ergon.studentmarks.api.generated.dto.UserRoleDto
import ch.ergon.studentmarks.application.query.model.ApprenticeSupervisorPairWithUsers
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPairId
import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.SimpleUser
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.model.user.UserName
import ch.ergon.studentmarks.domain.model.user.UserRole

fun UserRole.toDto(): UserRoleDto =
    when (this) {
        UserRole.ADMIN -> UserRoleDto.ADMIN
        UserRole.SUPERVISOR -> UserRoleDto.SUPERVISOR
        UserRole.APPRENTICE -> UserRoleDto.APPRENTICE
    }

fun UserRoleDto.toDomain(): UserRole =
    when (this) {
        UserRoleDto.ADMIN -> UserRole.ADMIN
        UserRoleDto.SUPERVISOR -> UserRole.SUPERVISOR
        UserRoleDto.APPRENTICE -> UserRole.APPRENTICE
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
        email = Email(email),
        role = role.toDomain(),
    )

fun SimpleUser.toDto(): SimpleUserDto =
    SimpleUserDto(
        email = email.value,
        userName = userName.value,
        role = role.toDto(),
    )

fun SimpleUserDto.toDomain(): SimpleUser =
    SimpleUser(
        email = Email(email),
        userName = UserName(userName),
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
