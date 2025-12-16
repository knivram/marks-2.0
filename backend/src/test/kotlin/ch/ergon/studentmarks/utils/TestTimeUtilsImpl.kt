package ch.ergon.studentmarks.utils

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@Primary
class TestTimeUtilsImpl : TimeUtils {
    private val defaultTime = LocalDate.of(2023, 5, 31)
    private var time = defaultTime

    override fun now(): LocalDate = time

    fun setTime(time: LocalDate) {
        this.time = time
    }

    fun setDefaultTime() {
        time = defaultTime
    }
}
