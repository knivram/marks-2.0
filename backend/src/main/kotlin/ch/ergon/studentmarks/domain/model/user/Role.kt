package ch.ergon.studentmarks.domain.model.user

import ch.ergon.studentmarks.domain.model.user.UserPermission.APPRENTICE_READ
import ch.ergon.studentmarks.domain.model.user.UserPermission.MARK_CREATE
import ch.ergon.studentmarks.domain.model.user.UserPermission.MARK_DELETE
import ch.ergon.studentmarks.domain.model.user.UserPermission.MARK_EDIT
import ch.ergon.studentmarks.domain.model.user.UserPermission.STUDENT_DATA_READ

enum class Role(
    val permissions: Set<UserPermission>,
) {
    ADMIN(setOf(STUDENT_DATA_READ, APPRENTICE_READ)),
    APPRENTICE(setOf(STUDENT_DATA_READ, MARK_CREATE, MARK_EDIT, MARK_DELETE)),
    SUPERVISOR(setOf(STUDENT_DATA_READ, APPRENTICE_READ)),
}
