package ch.ergon.studentmarks.infrastructure.repository

import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.UserRepository
import ch.ergon.studentmarks.gen.Tables.APPRENTICE_SUPERVISOR
import ch.ergon.studentmarks.gen.Tables.USER
import ch.ergon.studentmarks.infrastructure.repository.mapper.UserRecordMapper.toDB
import ch.ergon.studentmarks.infrastructure.repository.mapper.UserRecordMapper.toUser
import ch.ergon.studentmarks.domain.exception.UserAlreadyExistsException
import ch.ergon.studentmarks.domain.exception.UserNotFoundException
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

import ch.ergon.studentmarks.gen.enums.UserRole as DBUserRole

@Repository
class UserRepositoryImpl(
    private val dsl: DSLContext,
) : UserRepository {
    override fun getById(userId: UserId): User =
        dsl
            .selectFrom(USER)
            .where(USER.ID.eq(userId.value))
            .andNot(USER.DELETED)
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(userId.value.toString())

    override fun getByEmail(email: Email): User =
        dsl
            .selectFrom(USER)
            .where(USER.EMAIL.eq(email.value))
            .andNot(USER.DELETED)
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(email.value)

    override fun findByEmail(email: Email): User? =
        dsl
            .selectFrom(USER)
            .where(USER.EMAIL.eq(email.value))
            .andNot(USER.DELETED)
            .fetchOne()
            ?.toUser()

    override fun getAll(searchQuery: String): List<User> =
        dsl
            .select(USER.asterisk())
            .from(USER)
            .where(USER.EMAIL.likeIgnoreCase("%$searchQuery%").or(USER.USERNAME.likeIgnoreCase("%$searchQuery%")))
            .andNot(USER.DELETED)
            .orderBy(USER.USERNAME)
            .fetch()
            .map { it.toUser() }

    override fun create(user: User): User {
        try {
            return dsl
                .insertInto(USER)
                .set(USER.ID, user.userId.value)
                .set(USER.EMAIL, user.email.value)
                .set(USER.USERNAME, user.userName.value)
                .set(USER.ROLE, user.role.toDB())
                .returning()
                .fetchOne()!!
                .toUser()
        } catch (e: Exception) {
            throw UserAlreadyExistsException(
                user.email,
                user.userName,
            )
        }
    }

    override fun update(
        userId: UserId,
        user: User,
    ): User =
        dsl
            .update(USER)
            .set(USER.USERNAME, user.userName.value.lowercase())
            .set(USER.EMAIL, user.email.value.lowercase())
            .set(USER.ROLE, user.role.toDB())
            .where(USER.ID.eq(userId.value))
            .returning()
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(userId.value.toString())

    override fun softDelete(userId: UserId): User =
        dsl
            .update(USER)
            .set(USER.DELETED, true)
            .set(USER.DELETED_TIME, LocalDateTime.now())
            .where(USER.ID.eq(userId.value))
            .returning()
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(userId.value.toString())

    override fun restore(userId: UserId): User =
        dsl
            .update(USER)
            .set(USER.DELETED, false)
            .setNull(USER.DELETED_TIME)
            .where(USER.ID.eq(userId.value))
            .returning()
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(userId.value.toString())

    override fun findDeleted(): List<User> =
        dsl
            .selectFrom(USER)
            .where(USER.DELETED)
            .fetch()
            .map { it.toUser() }

    override fun permanentlyDelete(userId: UserId): User =
        dsl
            .deleteFrom(USER)
            .where(USER.ID.eq(userId.value))
            .and(USER.DELETED)
            .returning()
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(userId.value.toString())

    override fun findApprenticesForSupervisor(supervisor: User): List<User> {
        if (supervisor.isAdmin) {
            return dsl
                .selectFrom(USER)
                .where(USER.ROLE.eq(DBUserRole.APPRENTICE))
                .andNot(USER.DELETED)
                .fetch()
                .map { it.toUser() }
        } else {
            return dsl
                .select(USER.asterisk())
                .from(USER)
                .join(APPRENTICE_SUPERVISOR)
                .on(USER.ID.eq(APPRENTICE_SUPERVISOR.FK_APPRENTICE))
                .where(APPRENTICE_SUPERVISOR.FK_SUPERVISOR.eq(supervisor.userId.value))
                .andNot(USER.DELETED)
                .groupBy(USER.ID)
                .orderBy(USER.USERNAME)
                .fetch()
                .map { it.toUser() }
        }
    }

    override fun searchApprentices(searchQuery: String): List<User> =
        dsl
            .selectFrom(USER)
            .where(USER.ROLE.eq(DBUserRole.APPRENTICE))
            .and(USER.EMAIL.likeIgnoreCase("%$searchQuery%"))
            .andNot(USER.DELETED)
            .fetch()
            .map { it.toUser() }

    override fun searchSupervisors(searchQuery: String): List<User> =
        dsl
            .selectFrom(USER)
            .where(USER.ROLE.eq(DBUserRole.SUPERVISOR))
            .and(USER.EMAIL.likeIgnoreCase("%$searchQuery%"))
            .andNot(USER.DELETED)
            .fetch()
            .map { it.toUser() }

    override fun findSupervisorsWithNotificationsForApprentice(apprenticeId: UserId): List<User> =
        dsl
            .select(USER.asterisk())
            .from(USER)
            .join(APPRENTICE_SUPERVISOR)
            .on(USER.ID.eq(APPRENTICE_SUPERVISOR.FK_SUPERVISOR))
            .where(APPRENTICE_SUPERVISOR.FK_APPRENTICE.eq(apprenticeId.value))
            .and(APPRENTICE_SUPERVISOR.EMAIL_NOTIFICATION)
            .andNot(USER.DELETED)
            .groupBy(USER.ID)
            .fetch()
            .map { it.toUser() }
}
