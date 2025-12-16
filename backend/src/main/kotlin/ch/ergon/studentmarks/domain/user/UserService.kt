package ch.ergon.studentmarks.domain.user

import ch.ergon.studentmarks.db.UserRepository
import ch.ergon.studentmarks.infrastructure.security.exeptions.DeleteOwnUserException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getCurrentUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.principal as User
    }

    fun getCurrentUserOrNull(): User? = SecurityContextHolder.getContext().authentication.principal as User?

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getAllUser(searchQuery: String): List<User> = userRepository.getUsers(searchQuery)

    fun getUserByEmail(email: Email): User = userRepository.getUser(email)

    fun getUser(userId: UserId): User = userRepository.getUser(userId)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun addUser(simpleUser: SimpleUser) {
        userRepository.createUser(simpleUser)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun editUser(
        userId: UserId,
        user: User,
    ): User = userRepository.editUser(userId, user)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteUser(userId: UserId): User {
        if (userId == getCurrentUser().userId) {
            throw DeleteOwnUserException()
        }

        return userRepository.deleteUser(userId)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun restoreUser(userId: UserId): User = userRepository.restoreUser(userId)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun connectApprenticeToSupervisor(
        apprenticeEmail: Email,
        supervisorEmail: Email,
    ): ApprenticeSupervisorPair {
        val apprentice = getUserByEmail(apprenticeEmail)
        val supervisor = getUserByEmail(supervisorEmail)

        return userRepository.connectApprenticeToSupervisor(
            apprenticeId = apprentice.userId,
            supervisorId = supervisor.userId,
        )
    }

    @PreAuthorize("hasPermission(#supervisorId, 'IS_CURRENT_USER')")
    fun getApprentice(supervisorId: UserId): List<User> = userRepository.getApprentice(getUser(supervisorId))

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getMatchingApprentice(searchQuery: String): List<SimpleUser> = userRepository.getApprentice(searchQuery)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getMatchingSupervisor(searchQuery: String): List<SimpleUser> = userRepository.getSupervisors(searchQuery)

    @PreAuthorize("hasPermission(#supervisorId, 'IS_CURRENT_USER')")
    fun getApprenticeSupervisorPairs(supervisorId: UserId): List<ApprenticeSupervisorPair> =
        userRepository.getApprenticeSupervisorPairs(supervisorId, getUser(supervisorId).isAdmin)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getAllConnections(): List<ApprenticeSupervisorPair> = userRepository.getApprenticeSupervisorPairs()

    @PreAuthorize("hasPermission(#apprenticeSupervisorPair.supervisor.userId, 'IS_CURRENT_USER')")
    fun updateApprenticeSupervisorConnection(apprenticeSupervisorPair: ApprenticeSupervisorPair) {
        userRepository.updateOrInsertApprenticeSupervisorConnection(apprenticeSupervisorPair)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteConnection(apprenticeSupervisorPairId: ApprenticeSupervisorPairId): ApprenticeSupervisorPair =
        userRepository.deleteConnection(apprenticeSupervisorPairId)

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getDeletedUsers(): List<User> = userRepository.getDeletedUsers()

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteDeletedUser(userId: UserId): User = userRepository.deleteDeletedUser(userId)

    fun getNotificationEmailListForApprentice(apprenticeId: UserId): List<SimpleUser> =
        userRepository.getNotificationEmailListForApprentice(apprenticeId)
}
