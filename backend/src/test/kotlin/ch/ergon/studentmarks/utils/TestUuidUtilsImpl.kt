package ch.ergon.studentmarks.utils

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.UUID

@Primary
@Component
class TestUuidUtilsImpl : UuidUtils {
    private var uuid = UUID.randomUUID()

    fun setUUID(value: UUID) {
        uuid = value
    }

    override fun randomUUID(): UUID = uuid

    override fun fromString(string: String): UUID = UUID.fromString(string)
}
