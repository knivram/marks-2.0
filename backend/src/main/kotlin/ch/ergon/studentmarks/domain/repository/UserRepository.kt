package ch.ergon.studentmarks.domain.repository

import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId

interface UserRepository {
    fun findById(userId: UserId): User

    fun findByEmail(email: Email): User

    fun findByEmailOrNull(email: Email): User?

    fun findAll(searchQuery: String = ""): List<User>

    fun create(user: User): User

    fun update(
        userId: UserId,
        user: User,
    ): User

    fun softDelete(userId: UserId): User

    fun restore(userId: UserId): User

    fun findDeleted(): List<User>

    fun permanentlyDelete(userId: UserId): User

    fun findApprenticesForSupervisor(supervisor: User): List<User>

    fun searchApprentices(searchQuery: String): List<User>

    fun searchSupervisors(searchQuery: String): List<User>

    fun findSupervisorsWithNotificationsForApprentice(apprenticeId: UserId): List<User>
}
