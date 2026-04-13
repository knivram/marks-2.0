package ch.ergon.studentmarks.infrastructure

import com.fasterxml.jackson.databind.Module
import org.openapitools.jackson.nullable.JsonNullableModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfiguration {
    @Bean
    fun jsonNullableModule(): Module = JsonNullableModule()
}
