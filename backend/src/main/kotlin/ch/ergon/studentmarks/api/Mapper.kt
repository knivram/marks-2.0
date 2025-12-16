package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.api.semester.PaginatedListDto
import ch.ergon.studentmarks.api.semester.SemesterRequest
import ch.ergon.studentmarks.api.subjects.SubjectOverviewDto
import ch.ergon.studentmarks.api.subjects.SubjectRequest
import ch.ergon.studentmarks.api.user.ApprenticeSupervisorPairDto
import ch.ergon.studentmarks.api.user.SimpleUserDto
import ch.ergon.studentmarks.api.user.UserDto
import ch.ergon.studentmarks.api.user.UserRoleDto
import ch.ergon.studentmarks.domain.PaginatedList
import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.marks.SubjectId
import ch.ergon.studentmarks.domain.semester.Semester
import ch.ergon.studentmarks.domain.subjects.Subject
import ch.ergon.studentmarks.domain.subjects.SubjectOverview
import ch.ergon.studentmarks.domain.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.user.Email
import ch.ergon.studentmarks.domain.user.SimpleUser
import ch.ergon.studentmarks.domain.user.User
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.domain.user.UserName
import ch.ergon.studentmarks.domain.user.UserRole
import java.time.LocalDate

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

fun User.toSimpleUser(): SimpleUser =
    SimpleUser(
        email = email,
        userName = userName,
        role = role,
    )

fun User.toSimpleUserDto(): SimpleUserDto = toSimpleUser().toDto()

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

fun ApprenticeSupervisorPair.toDto(): ApprenticeSupervisorPairDto =
    ApprenticeSupervisorPairDto(
        id = id.value,
        apprentice = apprentice.toDto(),
        supervisor = supervisor.toDto(),
        emailNotification = emailNotification,
    )

fun ApprenticeSupervisorPairDto.toDomain() =
    ApprenticeSupervisorPair(
        id = id,
        apprentice = apprentice.toDomain(),
        supervisor = supervisor.toDomain(),
        emailNotification = emailNotification,
    )

fun SubjectOverview.toDto(): SubjectOverviewDto =
    SubjectOverviewDto(
        subjectId = subjectId.value,
        name = name,
        isVocationalModule = isVocationalModule,
        markAverage = markAverage.value,
        description = description,
    )

fun SubjectRequest.toDomain(subjectId: SubjectId): Subject =
    Subject(
        id = subjectId,
        name = name,
        isVocationalModule = isVocationalModule,
        description = description,
    )

fun SemesterRequest.toDomain(semesterId: SemesterId): Semester =
    Semester(
        id = semesterId,
        semesterName = semesterName,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
    )

fun <T> PaginatedList<T>.toDto(offset: Number): PaginatedListDto<T> =
    PaginatedListDto(
        totalEntries = totalEntries,
        offset = offset,
        list = this,
    )
