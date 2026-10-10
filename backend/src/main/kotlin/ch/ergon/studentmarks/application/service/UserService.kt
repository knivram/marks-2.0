package ch.ergon.studentmarks.application.service

import ch.ergon.studentmarks.application.security.ActingUserProvider
import ch.ergon.studentmarks.application.security.HasApprenticeReadPermission
import ch.ergon.studentmarks.application.security.HasStudentDataReadPermission
import ch.ergon.studentmarks.application.security.IsAdmin
import ch.ergon.studentmarks.application.security.NoPermissionRequired
import ch.ergon.studentmarks.application.security.TargetId
import ch.ergon.studentmarks.domain.exception.DeleteOwnUserException
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPairId
import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.Role
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.model.user.UserName
import ch.ergon.studentmarks.domain.repository.ApprenticeSupervisorRepository
import ch.ergon.studentmarks.domain.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val apprenticeSupervisorRepository: ApprenticeSupervisorRepository,
    private val actingUserProvider: ActingUserProvider,
) {
    @NoPermissionRequired("Everyone can load their own user")
    fun getCurrentUser() = actingUserProvider.actingPerson.user

    @IsAdmin
    fun getAllUser(searchQuery: String): List<User> = userRepository.getAll(searchQuery)

    @HasStudentDataReadPermission
    fun getUser(
        @TargetId userId: UserId,
    ): User = userRepository.getById(userId)

    @IsAdmin
    fun addUser(
        email: Email,
        userName: UserName,
        role: Role,
    ): User {
        val user = User.create(email, userName, role)
        return userRepository.create(user)
    }

    @IsAdmin
    fun editUser(
        userId: UserId,
        user: User,
    ): User = userRepository.update(userId, user)

    @IsAdmin
    fun deleteUser(userId: UserId): User {
        if (userId == actingUserProvider.actingPerson.user.userId) {
            throw DeleteOwnUserException()
        }
        return userRepository.softDelete(userId)
    }

    @IsAdmin
    fun restoreUser(userId: UserId): User = userRepository.restore(userId)

    // TODO: Arguments should probably be ids
    @IsAdmin
    fun connectApprenticeToSupervisor(
        apprenticeEmail: Email,
        supervisorEmail: Email,
    ): ApprenticeSupervisorPair {
        val apprentice = userRepository.getByEmail(apprenticeEmail)
        val supervisor = userRepository.getByEmail(supervisorEmail)
        return apprenticeSupervisorRepository.connect(
            apprenticeId = apprentice.userId,
            supervisorId = supervisor.userId,
        )
    }

    @HasApprenticeReadPermission
    fun getApprentice(
        // TODO: Consider using actingPerson directly
        @TargetId supervisorId: UserId,
    ): List<User> = userRepository.findApprenticesForSupervisor(userRepository.getById(supervisorId))

    @IsAdmin
    fun getMatchingApprentice(searchQuery: String): List<User> = userRepository.searchApprentices(searchQuery)

    @IsAdmin
    fun getMatchingSupervisor(searchQuery: String): List<User> = userRepository.searchSupervisors(searchQuery)

    @IsAdmin
    fun getApprenticeSupervisorPairs(supervisorId: UserId): List<ApprenticeSupervisorPair> =
        apprenticeSupervisorRepository.findBySupervisor(supervisorId, userRepository.getById(supervisorId).isAdmin)

    @IsAdmin
    fun getAllConnections(): List<ApprenticeSupervisorPair> = apprenticeSupervisorRepository.getAll()

    @IsAdmin
    fun updateApprenticeSupervisorConnection(pair: ApprenticeSupervisorPair) {
        apprenticeSupervisorRepository.updateOrInsert(pair)
    }

    @IsAdmin
    fun deleteConnection(pairId: ApprenticeSupervisorPairId): ApprenticeSupervisorPair = apprenticeSupervisorRepository.delete(pairId)

    @IsAdmin
    fun getDeletedUsers(): List<User> = userRepository.findDeleted()

    @IsAdmin
    fun deleteDeletedUser(userId: UserId): User = userRepository.permanentlyDelete(userId)
}
