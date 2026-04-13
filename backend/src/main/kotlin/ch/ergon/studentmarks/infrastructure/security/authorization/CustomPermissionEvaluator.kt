package ch.ergon.studentmarks.infrastructure.security.authorization

import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.domain.repository.UserRepository
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.io.Serializable

@Service
class CustomPermissionEvaluator(
    val markRepository: MarkRepository,
    val userRepository: UserRepository,
) : PermissionEvaluator {
    override fun hasPermission(
        authentication: Authentication?,
        targetDomainObject: Any?,
        permission: Any?,
    ): Boolean {
        val user = authentication?.principal as? User ?: return false
        return when (permission) {
            "IS_SUPERVISOR_OF" -> isSupervisorOf(targetDomainObject as UserId, user)
            "IS_CURRENT_USER" -> isCurrentUser(targetDomainObject as UserId, user)
            "IS_OWNER_OF" -> isOwnerOf(targetDomainObject as MarkId, user)
            else -> false
        }
    }

    override fun hasPermission(
        authentication: Authentication?,
        targetId: Serializable?,
        targetType: String?,
        permission: Any?,
    ): Boolean = throw NotImplementedError()

    fun isSupervisorOf(
        userId: UserId,
        currentUser: User,
    ): Boolean {
        val filteredApprentice =
            userRepository.findApprenticesForSupervisor(currentUser).singleOrNull { it.userId == userId }
        return filteredApprentice != null
    }

    fun isCurrentUser(
        userId: UserId,
        currentUser: User,
    ): Boolean = userId == currentUser.userId

    fun isOwnerOf(
        markId: MarkId,
        currentUser: User,
    ): Boolean {
        val markOwnerId = markRepository.getOwnerUserId(markId)
        return markOwnerId.value == currentUser.userId.value
    }
}
