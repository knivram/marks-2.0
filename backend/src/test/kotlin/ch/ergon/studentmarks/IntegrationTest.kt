package ch.ergon.studentmarks

import ch.ergon.studentmarks.utils.TestTimeUtilsImpl
import ch.ergon.studentmarks.utils.TestUuidUtilsImpl
import io.restassured.RestAssured
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest {
    @LocalServerPort
    private var localServerPort: Int? = null

    @Autowired
    private lateinit var flyway: Flyway

    @Autowired
    private lateinit var timeUtils: TestTimeUtilsImpl

    @Autowired
    private lateinit var testUuidUtilsImpl: TestUuidUtilsImpl

    @BeforeEach
    fun setBackendPort() {
        RestAssured.port = localServerPort!!
    }

    @BeforeEach
    fun setDatabaseToDefault() {
        flyway.clean()
        flyway.migrate()

        timeUtils.setDefaultTime()
        testUuidUtilsImpl.setUUID(UUID.randomUUID())
    }
}
