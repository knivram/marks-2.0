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
import java.net.Socket
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest {
    companion object {
        private val postgres =
            PostgreSQLContainer("postgres:14").apply {
                start()
            }

        init {
            waitUntilPortIsReachable(postgres.host, postgres.firstMappedPort)
            System.setProperty("spring.datasource.url", postgres.jdbcUrl)
            System.setProperty("spring.datasource.username", postgres.username)
            System.setProperty("spring.datasource.password", postgres.password)
        }

        private fun waitUntilPortIsReachable(
            host: String,
            port: Int,
            timeoutSeconds: Int = 10,
        ) {
            val deadline = System.currentTimeMillis() + timeoutSeconds.seconds.inWholeMilliseconds
            var lastFailure: Exception? = null

            while (System.currentTimeMillis() < deadline) {
                try {
                    Socket(host, port).use { return }
                } catch (e: Exception) {
                    lastFailure = e
                    Thread.sleep(100)
                }
            }

            throw IllegalStateException("PostgreSQL test container on $host:$port did not become reachable in time", lastFailure)
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
