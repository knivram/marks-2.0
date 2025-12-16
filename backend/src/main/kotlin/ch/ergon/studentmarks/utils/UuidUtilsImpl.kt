package ch.ergon.studentmarks.utils

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UuidUtilsImpl : UuidUtils {
    override fun randomUUID(): UUID = UUID.randomUUID()

    override fun fromString(string: String): UUID = UUID.fromString(string)
}
