package ch.ergon.studentmarks.infrastructure.repository.mapper

import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.SimpleUser
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.model.user.UserName
import ch.ergon.studentmarks.domain.model.user.UserRole
import ch.ergon.studentmarks.gen.Tables.USER
import org.jooq.Record
import ch.ergon.studentmarks.gen.enums.UserRole as DBUserRole

object UserRecordMapper {
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

    fun Record.toUser(): User =
        User(
            UserId(this[USER.ID]),
            UserName(this[USER.USERNAME]),
            Email(this[USER.EMAIL]),
            this[USER.ROLE].toDomain(),
        )

    fun Record.toSimpleUser(): SimpleUser =
        SimpleUser(
            Email(this[USER.EMAIL]),
            UserName(this[USER.USERNAME]),
            this[USER.ROLE].toDomain(),
        )
}
