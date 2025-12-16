package ch.ergon.studentmarks.utils

import java.util.UUID

interface UuidUtils {
    fun randomUUID(): UUID

    fun fromString(string: String): UUID
}
