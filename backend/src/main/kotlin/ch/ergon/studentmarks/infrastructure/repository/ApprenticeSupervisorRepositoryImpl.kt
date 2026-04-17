package ch.ergon.studentmarks.infrastructure.repository

import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPair
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPairId
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.ApprenticeSupervisorRepository
import ch.ergon.studentmarks.gen.Tables.APPRENTICE_SUPERVISOR
import ch.ergon.studentmarks.gen.Tables.USER
import ch.ergon.studentmarks.utils.UuidUtils
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.util.UUID
import ch.ergon.studentmarks.gen.enums.UserRole as DBUserRole

@Repository
class ApprenticeSupervisorRepositoryImpl(
    private val dsl: DSLContext,
    private val uuidUtils: UuidUtils,
) : ApprenticeSupervisorRepository {
    override fun connect(
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
                    ApprenticeSupervisorPairId(it.id),
                    UserId(it.fkApprentice),
                    UserId(it.fkSupervisor),
                    it.emailNotification,
                )
            }

    override fun findBySupervisor(
        supervisorId: UserId,
        includeUnconnectedForAdmin: Boolean,
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
            if (includeUnconnectedForAdmin) {
                adminQuery.unionAll(selectedApprentices)
            } else {
                selectedApprentices
            }

        return combinedQuery
            .orderBy(APPRENTICE_SUPERVISOR.ID)
            .fetch()
            .map {
                ApprenticeSupervisorPair(
                    ApprenticeSupervisorPairId(it.get(APPRENTICE_SUPERVISOR.ID) ?: uuidUtils.randomUUID()),
                    UserId(it[APPRENTICE_SUPERVISOR.FK_APPRENTICE]),
                    UserId(it[APPRENTICE_SUPERVISOR.FK_SUPERVISOR]),
                    it[APPRENTICE_SUPERVISOR.EMAIL_NOTIFICATION],
                )
            }
    }

    override fun getAll(): List<ApprenticeSupervisorPair> {
        val result =
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
                .fetch()

        return result.map {
            ApprenticeSupervisorPair(
                ApprenticeSupervisorPairId(it[APPRENTICE_SUPERVISOR.ID]),
                UserId(it[APPRENTICE_SUPERVISOR.FK_APPRENTICE]),
                UserId(it[APPRENTICE_SUPERVISOR.FK_SUPERVISOR]),
                it[APPRENTICE_SUPERVISOR.EMAIL_NOTIFICATION],
            )
        }
    }

    override fun updateOrInsert(pair: ApprenticeSupervisorPair) {
        dsl
            .insertInto(APPRENTICE_SUPERVISOR)
            .set(APPRENTICE_SUPERVISOR.ID, pair.id.value)
            .set(APPRENTICE_SUPERVISOR.FK_APPRENTICE, pair.apprenticeId.value)
            .set(APPRENTICE_SUPERVISOR.FK_SUPERVISOR, pair.supervisorId.value)
            .set(APPRENTICE_SUPERVISOR.EMAIL_NOTIFICATION, pair.emailNotification)
            .onConflict(APPRENTICE_SUPERVISOR.FK_APPRENTICE, APPRENTICE_SUPERVISOR.FK_SUPERVISOR)
            .doUpdate()
            .set(APPRENTICE_SUPERVISOR.FK_SUPERVISOR, pair.supervisorId.value)
            .set(APPRENTICE_SUPERVISOR.FK_APPRENTICE, pair.apprenticeId.value)
            .set(APPRENTICE_SUPERVISOR.EMAIL_NOTIFICATION, pair.emailNotification)
            .execute()
    }

    override fun delete(pairId: ApprenticeSupervisorPairId): ApprenticeSupervisorPair =
        dsl
            .deleteFrom(APPRENTICE_SUPERVISOR)
            .where(APPRENTICE_SUPERVISOR.ID.eq(pairId.value))
            .returning()
            .fetchOne()!!
            .let {
                ApprenticeSupervisorPair(
                    ApprenticeSupervisorPairId(it.id),
                    UserId(it.fkApprentice),
                    UserId(it.fkSupervisor),
                    it.emailNotification,
                )
            }
}
