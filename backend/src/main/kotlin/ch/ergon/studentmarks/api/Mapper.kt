package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.api.generated.dto.ApprenticeSupervisorPairDto
import ch.ergon.studentmarks.api.generated.dto.MarkDto
import ch.ergon.studentmarks.api.generated.dto.MarkValueDto
import ch.ergon.studentmarks.api.generated.dto.PaginatedSemesterListDto
import ch.ergon.studentmarks.api.generated.dto.SemesterDto
import ch.ergon.studentmarks.api.generated.dto.SemesterRequestDto
import ch.ergon.studentmarks.api.generated.dto.SimpleUserDto
import ch.ergon.studentmarks.api.generated.dto.SubjectDto
import ch.ergon.studentmarks.api.generated.dto.SubjectOverviewDto
import ch.ergon.studentmarks.api.generated.dto.SubjectRequestDto
import ch.ergon.studentmarks.api.generated.dto.UserDto
import ch.ergon.studentmarks.api.generated.dto.UserRoleDto
import ch.ergon.studentmarks.domain.PaginatedList
import ch.ergon.studentmarks.domain.marks.Mark
import ch.ergon.studentmarks.domain.marks.MarkValue
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

fun SubjectRequestDto.toDomain(subjectId: SubjectId): Subject =
    Subject(
        id = subjectId,
        name = name,
        isVocationalModule = isVocationalModule,
        description = description,
    )

fun SemesterRequestDto.toDomain(semesterId: SemesterId): Semester =
    Semester(
        id = semesterId,
        semesterName = semesterName,
        startDate = LocalDate.parse(startDate),
        endDate = LocalDate.parse(endDate),
    )

fun Subject.toDto(): SubjectDto =
    SubjectDto(
        id = id.value,
        name = name,
        isVocationalModule = isVocationalModule,
        description = description,
    )

fun Semester.toDto(): SemesterDto =
    SemesterDto(
        id = id.value,
        semesterName = semesterName,
        startDate = startDate,
        endDate = endDate,
    )

fun Mark.toDto(): MarkDto =
    MarkDto(
        id = id.value,
        name = name,
        subject = subject.toDto(),
        date = date,
        weight = weight,
        value = value.value,
        semesterId = semesterId.value,
    )

fun MarkValue.toDto(): MarkValueDto =
    MarkValueDto(value = value)

fun PaginatedList<Semester>.toPaginatedSemesterListDto(offset: Int): PaginatedSemesterListDto =
    PaginatedSemesterListDto(
        totalEntries = totalEntries,
        offset = offset,
        list = this.map { it.toDto() },
    )
