package ch.ergon.studentmarks.db

import ch.ergon.studentmarks.domain.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.user.ApprenticeSupervisorPairId
import ch.ergon.studentmarks.domain.user.Email
import ch.ergon.studentmarks.domain.user.SimpleUser
import ch.ergon.studentmarks.domain.user.User
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.domain.user.UserName
import ch.ergon.studentmarks.domain.user.UserRole
import ch.ergon.studentmarks.gen.Tables.APPRENTICE_SUPERVISOR
import ch.ergon.studentmarks.gen.Tables.USER
import ch.ergon.studentmarks.infrastructure.security.exeptions.UserAlreadyExistsException
import ch.ergon.studentmarks.infrastructure.security.exeptions.UserNotFoundException
import ch.ergon.studentmarks.utils.UuidUtils
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import ch.ergon.studentmarks.gen.enums.UserRole as DBUserRole

@Service
class UserRepository(
    private val dsl: DSLContext,
    private val uuidUtils: UuidUtils,
) {
    fun DBUserRole.toDomain(): UserRole =
        when (this) {
            DBUserRole.ADMIN -> UserRole.ADMIN
            DBUserRole.SUPERVISOR -> UserRole.SUPERVISOR
            DBUserRole.APPRENTICE -> UserRole.APPRENTICE
        }

    fun UserRole.toDB(): DBUserRole =
        when (this) {
            UserRole.ADMIN -> DBUserRole.ADMIN
            UserRole.SUPERVISOR -> DBUserRole.SUPERVISOR
            UserRole.APPRENTICE -> DBUserRole.APPRENTICE
        }

    private fun Record.toUser(): User =
        User(
            UserId(this[USER.ID]),
            UserName(this[USER.USERNAME]),
            Email(this[USER.EMAIL]),
            this[USER.ROLE].toDomain(),
        )

    private fun Record.toSimpleUser(): SimpleUser =
        SimpleUser(
            Email(this[USER.EMAIL]),
            UserName(this[USER.USERNAME]),
            this[USER.ROLE].toDomain(),
        )

    fun getUser(userId: UserId): User =
        dsl
            .selectFrom(USER)
            .where(USER.ID.eq(userId.value))
            .andNot(USER.DELETED)
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(userId.value.toString())

    fun getUser(mail: Email): User =
        dsl
            .selectFrom(USER)
            .where(USER.EMAIL.eq(mail.value))
            .andNot(USER.DELETED)
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(mail.value)

    fun getUsers(searchQuery: String = ""): List<User> =
        dsl
            .select(USER.asterisk())
            .from(USER)
            .where(USER.EMAIL.likeIgnoreCase("%$searchQuery%").or(USER.USERNAME.likeIgnoreCase("%$searchQuery%")))
            .andNot(USER.DELETED)
            .orderBy(USER.USERNAME)
            .fetch()
            .map { it.toUser() }

    fun getApprentice(supervisor: User): List<User> {
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

    fun getNotificationEmailListForApprentice(apprenticeId: UserId): List<SimpleUser> =
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

    fun editUser(
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

    fun getDeletedUsers(): List<User> =
        dsl
            .selectFrom(USER)
            .where(USER.DELETED)
            .fetch()
            .map { it.toUser() }

    fun createUser(simpleUser: SimpleUser): User {
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

    fun getApprentice(searchQuery: String = ""): List<SimpleUser> =
        dsl
            .selectFrom(USER)
            .where(USER.ROLE.eq(DBUserRole.APPRENTICE))
            .and(USER.EMAIL.likeIgnoreCase("%$searchQuery%"))
            .andNot(USER.DELETED)
            .fetch()
            .map { it.toSimpleUser() }

    fun getSupervisors(searchQuery: String = ""): List<SimpleUser> =
        dsl
            .selectFrom(USER)
            .where(USER.ROLE.eq(DBUserRole.SUPERVISOR))
            .and(USER.EMAIL.likeIgnoreCase("%$searchQuery%"))
            .andNot(USER.DELETED)
            .fetch()
            .map { it.toSimpleUser() }

    fun connectApprenticeToSupervisor(
        apprenticeId: UserId,
        supervisorId: UserId,
    ): ApprenticeSupervisorPair =
        dsl
            .insertInto(APPRENTICE_SUPERVISOR)
            .set(APPRENTICE_SUPERVISOR.FK_APPRENTICE, apprenticeId.value)
            .set(APPRENTICE_SUPERVISOR.FK_SUPERVISOR, supervisorId.value)
            .set(APPRENTICE_SUPERVISOR.ID, UUID.randomUUID())
            .returning()
            .fetchOne()!!
            .let {
                ApprenticeSupervisorPair(
                    it.id,
                    getUser(UserId(it.fkApprentice)),
                    getUser(UserId(it.fkSupervisor)),
                    it.emailNotification,
                )
            }

    fun getApprenticeSupervisorPairs(
        supervisorId: UserId,
        isAdmin: Boolean = false,
    ): List<ApprenticeSupervisorPair> {
        val selectedApprentices =
            dsl
                .select(
                    APPRENTICE_SUPERVISOR.ID,
                    APPRENTICE_SUPERVISOR.FK_APPRENTICE,
                    APPRENTICE_SUPERVISOR.FK_SUPERVISOR,
                    APPRENTICE_SUPERVISOR.EMAIL_NOTIFICATION,
                ).from(APPRENTICE_SUPERVISOR)
                .join(USER.`as`("ap"))
                .on(USER.`as`("ap").ID.eq(APPRENTICE_SUPERVISOR.FK_APPRENTICE))
                .join(USER.`as`("su"))
                .on(USER.`as`("su").ID.eq(APPRENTICE_SUPERVISOR.FK_SUPERVISOR))
                .where(USER.`as`("ap").DELETED.eq(false))
                .and(USER.`as`("su").DELETED.eq(false))
                .and(APPRENTICE_SUPERVISOR.FK_SUPERVISOR.eq(supervisorId.value))

        val adminQuery =
            dsl
                .select(
                    DSL.`val`(uuidUtils.randomUUID()).`as`("id"),
                    USER.ID.`as`("fk_apprentice"),
                    DSL.`val`(supervisorId.value).`as`("fk_supervisor"),
                    DSL.`val`(false).`as`("email_notification"),
                ).from(USER)
                .where(USER.ROLE.eq(DBUserRole.APPRENTICE))
                .andNot(USER.DELETED)
                .andNotExists(
                    DSL
                        .selectOne()
                        .from(APPRENTICE_SUPERVISOR)
                        .where(
                            APPRENTICE_SUPERVISOR.FK_APPRENTICE
                                .eq(USER.ID)
                                .and(APPRENTICE_SUPERVISOR.FK_SUPERVISOR.eq(supervisorId.value)),
                        ),
                )

        val combinedQuery =
            if (isAdmin) {
                adminQuery.unionAll(selectedApprentices)
            } else {
                selectedApprentices
            }

        return combinedQuery
            .orderBy(APPRENTICE_SUPERVISOR.ID)
            .fetch()
            .map {
                ApprenticeSupervisorPair(
                    it.get(APPRENTICE_SUPERVISOR.ID) ?: uuidUtils.randomUUID(),
                    getUser(UserId(it[APPRENTICE_SUPERVISOR.FK_APPRENTICE])),
                    getUser(UserId(it[APPRENTICE_SUPERVISOR.FK_SUPERVISOR])),
                    it[APPRENTICE_SUPERVISOR.EMAIL_NOTIFICATION],
                )
            }
    }

    fun getApprenticeSupervisorPairs(): List<ApprenticeSupervisorPair> {
        val result =
            dsl
                .select(
                    APPRENTICE_SUPERVISOR.ID,
                    USER.`as`("ap").ID,
                    USER.`as`("su").ID,
                    APPRENTICE_SUPERVISOR.EMAIL_NOTIFICATION,
                ).from(APPRENTICE_SUPERVISOR)
                .join(USER.`as`("ap"))
                .on(USER.`as`("ap").ID.eq(APPRENTICE_SUPERVISOR.FK_APPRENTICE))
                .join(USER.`as`("su"))
                .on(USER.`as`("su").ID.eq(APPRENTICE_SUPERVISOR.FK_SUPERVISOR))
                .where(USER.`as`("ap").DELETED.eq(false))
                .and(USER.`as`("su").DELETED.eq(false))
                .fetch()

        return result.map {
            ApprenticeSupervisorPair(
                it.value1(),
                getUser(UserId(it.value2())),
                getUser(UserId(it.value3())),
                it.value4(),
            )
        }
    }

    fun deleteConnection(apprenticeSupervisorPairId: ApprenticeSupervisorPairId): ApprenticeSupervisorPair =
        dsl
            .deleteFrom(APPRENTICE_SUPERVISOR)
            .where(APPRENTICE_SUPERVISOR.ID.eq(apprenticeSupervisorPairId.value))
            .returning()
            .fetchOne()!!
            .let {
                ApprenticeSupervisorPair(
                    it.id,
                    getUser(UserId(it.fkApprentice)),
                    getUser(UserId(it.fkSupervisor)),
                    it.emailNotification,
                )
            }

    fun updateOrInsertApprenticeSupervisorConnection(apprenticeSupervisorPair: ApprenticeSupervisorPair) {
        dsl
            .insertInto(APPRENTICE_SUPERVISOR)
            .set(APPRENTICE_SUPERVISOR.ID, apprenticeSupervisorPair.id.value)
            .set(APPRENTICE_SUPERVISOR.FK_APPRENTICE, apprenticeSupervisorPair.apprentice.userId.value)
            .set(APPRENTICE_SUPERVISOR.FK_SUPERVISOR, apprenticeSupervisorPair.supervisor.userId.value)
            .set(APPRENTICE_SUPERVISOR.EMAIL_NOTIFICATION, apprenticeSupervisorPair.emailNotification)
            .onConflict(APPRENTICE_SUPERVISOR.FK_APPRENTICE, APPRENTICE_SUPERVISOR.FK_SUPERVISOR)
            .doUpdate()
            .set(APPRENTICE_SUPERVISOR.FK_SUPERVISOR, apprenticeSupervisorPair.supervisor.userId.value)
            .set(APPRENTICE_SUPERVISOR.FK_APPRENTICE, apprenticeSupervisorPair.apprentice.userId.value)
            .set(APPRENTICE_SUPERVISOR.EMAIL_NOTIFICATION, apprenticeSupervisorPair.emailNotification)
            .execute()
    }

    fun deleteUser(userId: UserId): User =
        dsl
            .update(USER)
            .set(USER.DELETED, true)
            .set(USER.DELETED_TIME, LocalDateTime.now())
            .where(USER.ID.eq(userId.value))
            .returning()
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(userId.value.toString())

    fun restoreUser(userId: UserId): User =
        dsl
            .update(USER)
            .set(USER.DELETED, false)
            .setNull(USER.DELETED_TIME)
            .where(USER.ID.eq(userId.value))
            .returning()
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(userId.value.toString())

    fun deleteDeletedUser(userId: UserId): User =
        dsl
            .deleteFrom(USER)
            .where(USER.ID.eq(userId.value))
            .and(USER.DELETED)
            .returning()
            .fetchOne()
            ?.toUser()
            ?: throw UserNotFoundException(userId.value.toString())
}
