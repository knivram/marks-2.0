package ch.ergon.studentmarks.application.service

import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPairId
import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.SimpleUser
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.ApprenticeSupervisorRepository
import ch.ergon.studentmarks.domain.repository.UserRepository
import ch.ergon.studentmarks.domain.exception.DeleteOwnUserException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val apprenticeSupervisorRepository: ApprenticeSupervisorRepository,
) {
    fun getCurrentUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.principal as User
    }

    fun getCurrentUserOrNull(): User? =
        try {
            getCurrentUser()
        } catch (_: Exception) {
            null
        }

    fun resolveUserId(userId: UserId?): UserId = userId ?: getCurrentUser().userId

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getAllUser(searchQuery: String): List<User> = userRepository.findAll(searchQuery)

    fun getUserByEmail(email: Email): User = userRepository.findByEmail(email)

    fun getUser(userId: UserId): User = userRepository.findById(userId)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun addUser(simpleUser: SimpleUser) {
        userRepository.create(simpleUser)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun editUser(
        userId: UserId,
        user: User,
    ): User = userRepository.update(userId, user)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteUser(userId: UserId): User {
        if (userId == getCurrentUser().userId) {
            throw DeleteOwnUserException()
        }
        return userRepository.softDelete(userId)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun restoreUser(userId: UserId): User = userRepository.restore(userId)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun connectApprenticeToSupervisor(
        apprenticeEmail: Email,
        supervisorEmail: Email,
    ): ApprenticeSupervisorPair {
        val apprentice = getUserByEmail(apprenticeEmail)
        val supervisor = getUserByEmail(supervisorEmail)
        return apprenticeSupervisorRepository.connect(
            apprenticeId = apprentice.userId,
            supervisorId = supervisor.userId,
        )
    }

    @PreAuthorize("hasPermission(#supervisorId, 'IS_CURRENT_USER')")
    fun getApprentice(supervisorId: UserId): List<User> = userRepository.findApprenticesForSupervisor(getUser(supervisorId))

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getMatchingApprentice(searchQuery: String): List<SimpleUser> = userRepository.searchApprentices(searchQuery)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getMatchingSupervisor(searchQuery: String): List<SimpleUser> = userRepository.searchSupervisors(searchQuery)

    @PreAuthorize("hasPermission(#supervisorId, 'IS_CURRENT_USER')")
    fun getApprenticeSupervisorPairs(supervisorId: UserId): List<ApprenticeSupervisorPair> =
        apprenticeSupervisorRepository.findBySupervisor(supervisorId, getUser(supervisorId).isAdmin)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getAllConnections(): List<ApprenticeSupervisorPair> = apprenticeSupervisorRepository.findAll()

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun updateApprenticeSupervisorConnection(pair: ApprenticeSupervisorPair) {
        apprenticeSupervisorRepository.updateOrInsert(pair)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteConnection(pairId: ApprenticeSupervisorPairId): ApprenticeSupervisorPair = apprenticeSupervisorRepository.delete(pairId)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDeletedUsers(): List<User> = userRepository.findDeleted()

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteDeletedUser(userId: UserId): User = userRepository.permanentlyDelete(userId)

    fun getNotificationEmailListForApprentice(apprenticeId: UserId): List<SimpleUser> =
        userRepository.findSupervisorsWithNotificationsForApprentice(apprenticeId)
}
