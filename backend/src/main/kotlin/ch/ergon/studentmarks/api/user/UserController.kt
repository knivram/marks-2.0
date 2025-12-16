package ch.ergon.studentmarks.api.user

import ch.ergon.studentmarks.api.toDomain
import ch.ergon.studentmarks.api.toDto
import ch.ergon.studentmarks.domain.user.ApprenticeSupervisorPairId
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.domain.user.UserService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    val userService: UserService,
) {
    @GetMapping
    fun returnAllUsers(
        @RequestParam(defaultValue = "") searchQuery: String,
    ): List<UserDto> = userService.getAllUser(searchQuery).map { it.toDto() }

    @PostMapping
    fun addUser(
        @RequestBody simpleUser: SimpleUserDto,
    ) {
        userService.addUser(simpleUser.toDomain())
    }

    @PutMapping("/{id}/edit")
    fun editUser(
        @PathVariable(name = "id") userId: UserId,
        @RequestBody userDto: UserDto,
    ): UserDto = userService.editUser(userId, userDto.toDomain()).toDto()

    @DeleteMapping("/{id}")
    fun deleteUser(
        @PathVariable(name = "id") userId: UserId,
    ): UserDto = userService.deleteUser(userId).toDto()

    @PostMapping("/{id}/restore")
    fun restoreUser(
        @PathVariable(name = "id") userId: UserId,
    ): UserDto = userService.restoreUser(userId).toDto()

    @GetMapping("/deletedUsers")
    fun getDeletedUsers(): List<UserDto> = userService.getDeletedUsers().map { it.toDto() }

    @DeleteMapping("/deletedUsers/{id}")
    fun deleteDeletedUser(
        @PathVariable(name = "id") userId: UserId,
    ): UserDto = userService.deleteDeletedUser(userId).toDto()

    @GetMapping("/info")
    fun userInfo(): UserDto = userService.getCurrentUser().toDto()

    @PostMapping("/connect")
    fun connect(
        @RequestBody request: ApprenticeConnectRequest,
    ): ApprenticeSupervisorPairDto = userService.connectApprenticeToSupervisor(request.apprenticeEmail, request.supervisorEmail).toDto()

    @GetMapping("/{id}/apprentice")
    fun getApprentice(
        @PathVariable(name = "id") supervisorId: UserId,
    ): List<UserDto> = userService.getApprentice(supervisorId).map { it.toDto() }

    @GetMapping("/connections")
    fun getConnections(
        @RequestParam(
            defaultValue = "",
            name = "supervisor-id",
        ) supervisorId: String,
    ): List<ApprenticeSupervisorPairDto> =
        if (supervisorId.isBlank()) {
            userService.getAllConnections().map { it.toDto() }
        } else {
            userService.getApprenticeSupervisorPairs(UserId(supervisorId)).map { it.toDto() }
        }

    @PutMapping("/connections")
    fun updateConnections(
        @RequestBody apprenticeSupervisorPairDtos: List<ApprenticeSupervisorPairDto>,
    ): List<ApprenticeSupervisorPairDto> {
        if (apprenticeSupervisorPairDtos.isEmpty()) {
            return emptyList()
        }
        apprenticeSupervisorPairDtos.forEach { userService.updateApprenticeSupervisorConnection(it.toDomain()) }
        return userService
            .getApprenticeSupervisorPairs(UserId(apprenticeSupervisorPairDtos.first().supervisor.userId))
            .map { it.toDto() }
    }

    @DeleteMapping("/connections/{id}")
    fun deleteConnection(
        @PathVariable id: ApprenticeSupervisorPairId,
    ): ApprenticeSupervisorPairDto = userService.deleteConnection(id).toDto()

    @GetMapping("/apprentice")
    fun returnMatchingApprentice(
        @RequestParam(defaultValue = "") searchQuery: String,
    ): List<SimpleUserDto> = userService.getMatchingApprentice(searchQuery).map { it.toDto() }

    @GetMapping("/supervisor")
    fun returnMatchingSupervisor(
        @RequestParam(defaultValue = "") searchQuery: String,
    ): List<SimpleUserDto> = userService.getMatchingSupervisor(searchQuery).map { it.toDto() }
}
