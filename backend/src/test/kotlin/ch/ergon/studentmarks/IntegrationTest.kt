package ch.ergon.studentmarks

import ch.ergon.studentmarks.utils.TestTimeUtilsImpl
import ch.ergon.studentmarks.utils.TestUuidUtilsImpl
import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured
import io.restassured.config.ObjectMapperConfig
import io.restassured.config.RestAssuredConfig
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.testcontainers.containers.PostgreSQLContainer
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest {
    companion object {
        init {
            PostgreSQLContainer("postgres:14").apply {
                start()
                System.setProperty("spring.datasource.url", jdbcUrl)
                System.setProperty("spring.datasource.username", username)
                System.setProperty("spring.datasource.password", password)
            }
        }
    }

    @LocalServerPort
    private var localServerPort: Int? = null

    @Autowired
    private lateinit var flyway: Flyway

    @Autowired
    private lateinit var timeUtils: TestTimeUtilsImpl

    @Autowired
    private lateinit var testUuidUtilsImpl: TestUuidUtilsImpl

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setBackendPort() {
        RestAssured.port = localServerPort!!
        RestAssured.config =
            RestAssuredConfig.config().objectMapperConfig(
                ObjectMapperConfig().jackson2ObjectMapperFactory { _, _ -> objectMapper },
            )
    }

    @BeforeEach
    fun setDatabaseToDefault() {
        flyway.clean()
        flyway.migrate()

        timeUtils.setDefaultTime()
        testUuidUtilsImpl.setUUID(UUID.randomUUID())
    }
}
