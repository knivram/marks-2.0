import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.springframework.boot") version "3.0.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("nu.studer.jooq") version "8.0"
    id("org.flywaydb.flyway") version "9.7.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("org.openapi.generator") version "7.12.0"
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.spring") version "1.9.10"
}

val gradlePostgresHost = project.properties["POSTGRES_HOST"] ?: "localhost:3134"

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-gson:0.11.5")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.flywaydb:flyway-core:9.10.0")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("com.google.code.gson:gson:2.10")

    implementation("io.swagger.core.v3:swagger-annotations:2.2.28")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot-starter-jooq")

    runtimeOnly("org.postgresql:postgresql:42.5.1")
    jooqGenerator("org.postgresql:postgresql:42.5.1")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

flyway {
    url = "jdbc:postgresql://$gradlePostgresHost/gradlecodegen"
    user = "gradle"
    password = "password"
    locations = arrayOf("filesystem:$rootDir/backend/src/main/resources/db/migration")
    cleanDisabled = false
}

jooq {
    configurations {
        create("main") {
            // name of the jOOQ configuration
            generateSchemaSourceOnCompilation.set(true) // default (can be omitted)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://$gradlePostgresHost/gradlecodegen"
                    user = "gradle"
                    password = "password"
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        inputSchema = "public"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                        isRelations = true
                        isDaos = true
                    }
                    target.apply {
                        packageName = "ch.ergon.studentmarks.gen"
                        directory = "$rootDir/backend/src/gen/java"
                    }
                }
            }
        }
    }
}

tasks["generateJooq"].dependsOn(tasks["flywayMigrate"])

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    debug.set(true)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    filter {
        exclude("**/generated/**")
    }
}

val generateKotlinOpenApi = tasks.register("generateKotlinOpenApi", GenerateTask::class.java) {
    inputSpec.set("$projectDir/src/main/resources/openapi/api.yaml")
    inputs.dir("$projectDir/src/main/resources/openapi/")
    generatorName.set("kotlin-spring")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").map { it.asFile.absolutePath })
    apiPackage.set("ch.ergon.studentmarks.api.generated")
    modelPackage.set("ch.ergon.studentmarks.api.generated.dto")
    modelNameSuffix.set("Dto")
    configOptions.set(
        mapOf(
            "useSpringBoot3" to "true",
            "useTags" to "true",
            "skipDefaultInterface" to "true",
            "interfaceOnly" to "true",
            "useBeanValidation" to "false",
            "enumPropertyNaming" to "UPPERCASE",
        ),
    )
    templateDir.set("$rootDir/config/openapi/templates/kotlin-spring")
    generateModelTests.set(false)
    generateModelDocumentation.set(false)
    generateApiTests.set(false)
    generateApiDocumentation.set(false)
    skipValidateSpec.set(true)
}

kotlin {
    sourceSets["main"].kotlin.srcDir(
        generateKotlinOpenApi.map { file(it.outputDir).resolve("src/main/kotlin") },
    )
}

tasks.named("compileKotlin") {
    dependsOn(generateKotlinOpenApi)
}
