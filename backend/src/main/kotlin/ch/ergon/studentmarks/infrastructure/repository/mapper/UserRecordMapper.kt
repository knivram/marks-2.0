package ch.ergon.studentmarks.infrastructure.repository.mapper

import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.Role
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.model.user.UserName
import ch.ergon.studentmarks.gen.Tables.USER
import org.jooq.Record
import ch.ergon.studentmarks.gen.enums.UserRole as DBUserRole

object UserRecordMapper {
    fun DBUserRole.toDomain(): Role =
        when (this) {
            DBUserRole.ADMIN -> Role.ADMIN
            DBUserRole.SUPERVISOR -> Role.SUPERVISOR
            DBUserRole.APPRENTICE -> Role.APPRENTICE
        }

    fun Role.toDB(): DBUserRole =
        when (this) {
            Role.ADMIN -> DBUserRole.ADMIN
            Role.SUPERVISOR -> DBUserRole.SUPERVISOR
            Role.APPRENTICE -> DBUserRole.APPRENTICE
        }

    fun Record.toUser(): User =
        User(
            UserId(this[USER.ID]),
            UserName(this[USER.USERNAME]),
            Email.create(this[USER.EMAIL]),
            this[USER.ROLE].toDomain(),
        )
}
