package ch.ergon.studentmarks.application.security

import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.domain.repository.UserRepository
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * Resolves the user owning the resource behind a [TargetId], so a [GrantedTo.usersWithAnyOf] clause can check whether
 * the acting user can reach that owner. One implementation per [idType]. An unknown id throws the resource's
 * not-found exception.
 */
interface TargetUserResolver<ID : Any> {
    val idType: KClass<ID>

    fun resolveUserId(targetId: ID): UserId

    fun resolve(targetId: Any): UserId = resolveUserId(idType.cast(targetId))
}

@Component
class UserIdTargetResolver(
    private val userRepository: UserRepository,
) : TargetUserResolver<UserId> {
    override val idType = UserId::class

    override fun resolveUserId(targetId: UserId): UserId = userRepository.getById(targetId).userId
}

@Component
class MarkIdTargetResolver(
    private val markRepository: MarkRepository,
) : TargetUserResolver<MarkId> {
    override val idType = MarkId::class

    override fun resolveUserId(targetId: MarkId): UserId = markRepository.getOwnerUserId(targetId)
}
