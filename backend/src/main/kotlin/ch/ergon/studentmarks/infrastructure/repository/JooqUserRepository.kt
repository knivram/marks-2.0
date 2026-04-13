package ch.ergon.studentmarks.infrastructure.repository

import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.SimpleUser
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.UserRepository
import ch.ergon.studentmarks.gen.Tables.APPRENTICE_SUPERVISOR
import ch.ergon.studentmarks.gen.Tables.USER
import ch.ergon.studentmarks.infrastructure.repository.mapper.UserRecordMapper.toDB
import ch.ergon.studentmarks.infrastructure.repository.mapper.UserRecordMapper.toSimpleUser
import ch.ergon.studentmarks.infrastructure.repository.mapper.UserRecordMapper.toUser
import ch.ergon.studentmarks.infrastructure.security.exeptions.UserAlreadyExistsException
import ch.ergon.studentmarks.infrastructure.security.exeptions.UserNotFoundException
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID
import ch.ergon.studentmarks.gen.enums.UserRole as DBUserRole

@Repository
class JooqUserRepository(
    private val dsl: DSLContext,
) : UserRepository {
    override fun findById(userId: UserId): User =
        dsl
            .selectFrom(USER)
            .where(USER.ID.eq(userId.value))
            .andNot(USER.DELETED)
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(userId.value.toString())

    override fun findByEmail(email: Email): User =
        dsl
            .selectFrom(USER)
            .where(USER.EMAIL.eq(email.value))
            .andNot(USER.DELETED)
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(email.value)

    override fun findByEmailOrNull(email: Email): User? =
        dsl
            .selectFrom(USER)
            .where(USER.EMAIL.eq(email.value))
            .andNot(USER.DELETED)
            .fetchOne()
            ?.toUser()

    override fun findAll(searchQuery: String): List<User> =
        dsl
            .select(USER.asterisk())
            .from(USER)
            .where(USER.EMAIL.likeIgnoreCase("%$searchQuery%").or(USER.USERNAME.likeIgnoreCase("%$searchQuery%")))
            .andNot(USER.DELETED)
            .orderBy(USER.USERNAME)
            .fetch()
            .map { it.toUser() }

    override fun create(simpleUser: SimpleUser): User {
        val userId = UUID.randomUUID()
        try {
            return dsl
                .insertInto(USER)
                .set(USER.ID, userId)
                .set(USER.EMAIL, simpleUser.email.value.lowercase())
                .set(USER.USERNAME, simpleUser.userName.value.lowercase())
                .set(USER.ROLE, simpleUser.role.toDB())
                .returning()
                .fetchOne()!!
                .toUser()
        } catch (e: Exception) {
            throw UserAlreadyExistsException(
                simpleUser.email,
                simpleUser.userName,
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

    override fun searchApprentices(searchQuery: String): List<SimpleUser> =
        dsl
            .selectFrom(USER)
            .where(USER.ROLE.eq(DBUserRole.APPRENTICE))
            .and(USER.EMAIL.likeIgnoreCase("%$searchQuery%"))
            .andNot(USER.DELETED)
            .fetch()
            .map { it.toSimpleUser() }

    override fun searchSupervisors(searchQuery: String): List<SimpleUser> =
        dsl
            .selectFrom(USER)
            .where(USER.ROLE.eq(DBUserRole.SUPERVISOR))
            .and(USER.EMAIL.likeIgnoreCase("%$searchQuery%"))
            .andNot(USER.DELETED)
            .fetch()
            .map { it.toSimpleUser() }

    override fun findSupervisorsWithNotificationsForApprentice(apprenticeId: UserId): List<SimpleUser> =
        dsl
            .select(USER.EMAIL, USER.USERNAME, USER.ROLE)
            .from(USER)
            .join(APPRENTICE_SUPERVISOR)
            .on(USER.ID.eq(APPRENTICE_SUPERVISOR.FK_SUPERVISOR))
            .where(APPRENTICE_SUPERVISOR.FK_APPRENTICE.eq(apprenticeId.value))
            .and(APPRENTICE_SUPERVISOR.EMAIL_NOTIFICATION)
            .andNot(USER.DELETED)
            .groupBy(USER.EMAIL, USER.USERNAME, USER.ROLE)
            .fetch()
            .map { it.toSimpleUser() }
}
