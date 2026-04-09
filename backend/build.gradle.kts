import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Property
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.springframework.boot") version "3.5.13"
    id("io.spring.dependency-management") version "1.1.7"
    id("nu.studer.jooq") version "9.0"
    id("org.flywaydb.flyway") version "11.20.3"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("org.openapi.generator") version "7.21.0"
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.spring") version "2.3.20"
}

val gradlePostgresHost = project.properties["POSTGRES_HOST"] ?: "localhost:3134"

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    implementation("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-gson:0.13.0")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.4")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.7")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("com.google.code.gson:gson:2.13.2")

    implementation("io.swagger.core.v3:swagger-annotations:2.2.46")
    implementation("org.openapitools:jackson-databind-nullable:0.2.10")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot-starter-jooq")

    runtimeOnly("org.postgresql:postgresql:42.7.10")
    jooqGenerator("org.jooq:jooq-meta-extensions:3.19.31")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
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
    version.set("3.19.31")
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                logging = Logging.WARN
                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                        inputSchema = "public"
                        withProperties(
                            Property()
                                .withKey("scripts")
                                .withValue("src/main/resources/db/migration/V*.sql"),
                            Property()
                                .withKey("sort")
                                .withValue("flyway"),
                            Property()
                                .withKey("unqualifiedSchema")
                                .withValue("public"),
                            Property()
                                .withKey("defaultNameCase")
                                .withValue("lower"),
                        )
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                        isRelations = true
                        isDaos = true
                        isImplicitJoinPathsToOne = false
                        isImplicitJoinPathsToMany = false
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

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.8.0")
    debug.set(true)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    filter {
        exclude("**/generated/**")
        exclude { it.file.path.contains("/build/") }
    }
}

val generateKotlinOpenApi =
    tasks.register("generateKotlinOpenApi", GenerateTask::class.java) {
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
                "documentationProvider" to "none",
            ),
        )
        templateDir.set("$projectDir/config/openapi/templates/kotlin-spring")
        generateModelTests.set(false)
        generateModelDocumentation.set(false)
        generateApiTests.set(false)
        generateApiDocumentation.set(false)
        skipValidateSpec.set(true)
    }

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
    sourceSets["main"].kotlin.srcDir(
        generateKotlinOpenApi.map { file(it.outputDir).resolve("src/main/kotlin") },
    )
}

tasks.named("compileKotlin") {
    dependsOn(generateKotlinOpenApi)
}
