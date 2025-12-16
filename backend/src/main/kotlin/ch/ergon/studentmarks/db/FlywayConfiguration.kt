package ch.ergon.studentmarks.db

import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywayConfiguration(
    private val dataSource: DataSource,
) {
    @Bean(initMethod = "migrate")
    fun flyway(): Flyway =
        Flyway
            .configure()
            .dataSource(dataSource)
            .cleanDisabled(false)
            .load()
}
