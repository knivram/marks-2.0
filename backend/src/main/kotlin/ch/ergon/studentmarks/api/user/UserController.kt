package ch.ergon.studentmarks.api.user

import ch.ergon.studentmarks.api.generated.UsersApi
import ch.ergon.studentmarks.api.generated.dto.ApprenticeConnectRequestDto
import ch.ergon.studentmarks.api.generated.dto.ApprenticeSupervisorPairDto
import ch.ergon.studentmarks.api.generated.dto.SimpleUserDto
import ch.ergon.studentmarks.api.generated.dto.UserDto
import ch.ergon.studentmarks.api.toDomain
import ch.ergon.studentmarks.api.toDto
import ch.ergon.studentmarks.domain.user.ApprenticeSupervisorPairId
import ch.ergon.studentmarks.domain.user.Email
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.domain.user.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class UserController(
    val userService: UserService,
) : UsersApi {
    override fun returnAllUsers(searchQuery: String): ResponseEntity<List<UserDto>> =
        ResponseEntity.ok(userService.getAllUser(searchQuery).map { it.toDto() })

    override fun addUser(body: SimpleUserDto): ResponseEntity<Unit> {
        userService.addUser(body.toDomain())
        return ResponseEntity.noContent().build()
    }

    override fun editUser(
        id: UUID,
        userDto: UserDto,
    ): ResponseEntity<UserDto> = ResponseEntity.ok(userService.editUser(UserId(id), userDto.toDomain()).toDto())

    override fun deleteUser(id: UUID): ResponseEntity<UserDto> = ResponseEntity.ok(userService.deleteUser(UserId(id)).toDto())

    override fun restoreUser(id: UUID): ResponseEntity<UserDto> = ResponseEntity.ok(userService.restoreUser(UserId(id)).toDto())

    override fun getDeletedUsers(): ResponseEntity<List<UserDto>> = ResponseEntity.ok(userService.getDeletedUsers().map { it.toDto() })

    override fun deleteDeletedUser(id: UUID): ResponseEntity<UserDto> = ResponseEntity.ok(userService.deleteDeletedUser(UserId(id)).toDto())

    override fun userInfo(): ResponseEntity<UserDto> = ResponseEntity.ok(userService.getCurrentUser().toDto())

    override fun connect(body: ApprenticeConnectRequestDto): ResponseEntity<ApprenticeSupervisorPairDto> =
        ResponseEntity.ok(
            userService
                .connectApprenticeToSupervisor(
                    Email(body.apprenticeEmail),
                    Email(body.supervisorEmail),
                ).toDto(),
        )

    override fun getApprentice(id: UUID): ResponseEntity<List<UserDto>> =
        ResponseEntity.ok(userService.getApprentice(UserId(id)).map { it.toDto() })

    override fun getConnections(supervisorId: String): ResponseEntity<List<ApprenticeSupervisorPairDto>> =
        ResponseEntity.ok(
            if (supervisorId.isBlank()) {
                userService.getAllConnections().map { it.toDto() }
            } else {
                userService.getApprenticeSupervisorPairs(UserId(supervisorId)).map { it.toDto() }
            },
        )

    override fun updateConnections(
        apprenticeSupervisorPairDto: List<ApprenticeSupervisorPairDto>,
    ): ResponseEntity<List<ApprenticeSupervisorPairDto>> {
        if (apprenticeSupervisorPairDto.isEmpty()) {
            return ResponseEntity.ok(emptyList())
        }
        apprenticeSupervisorPairDto.forEach { userService.updateApprenticeSupervisorConnection(it.toDomain()) }
        return ResponseEntity.ok(
            userService
                .getApprenticeSupervisorPairs(UserId(apprenticeSupervisorPairDto.first().supervisor.userId))
                .map { it.toDto() },
        )
    }

    override fun deleteConnection(id: UUID): ResponseEntity<ApprenticeSupervisorPairDto> =
        ResponseEntity.ok(userService.deleteConnection(ApprenticeSupervisorPairId(id)).toDto())

    override fun returnMatchingApprentice(searchQuery: String): ResponseEntity<List<SimpleUserDto>> =
        ResponseEntity.ok(userService.getMatchingApprentice(searchQuery).map { it.toDto() })

    override fun returnMatchingSupervisor(searchQuery: String): ResponseEntity<List<SimpleUserDto>> =
        ResponseEntity.ok(userService.getMatchingSupervisor(searchQuery).map { it.toDto() })
}
