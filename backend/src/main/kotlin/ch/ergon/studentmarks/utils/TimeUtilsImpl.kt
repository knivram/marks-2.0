package ch.ergon.studentmarks.utils

import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class TimeUtilsImpl : TimeUtils {
    override fun now(): LocalDate = LocalDate.now()
}
