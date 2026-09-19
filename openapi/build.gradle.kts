plugins {
    kotlin("jvm") version "1.9.10"
    id("ch.ergon.marks.openapi")
}

repositories {
    mavenCentral()
}

// Configure where TypeScript should be generated
openApiConfig {
    typeScriptOutputDir.set(
        rootProject.layout.projectDirectory
            .dir("frontend/src/api/generated")
            .asFile.absolutePath
    )
}

dependencies {
    // Dependencies needed for generated Kotlin code
    implementation("org.springframework.boot:spring-boot-starter-web:3.0.0")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.0")
}

// Make compileKotlin depend on generation
tasks.named("compileKotlin") {
    dependsOn("generateKotlinOpenApi")
}

// Convenience task to generate both
tasks.register("generateAll") {
    dependsOn("generateKotlinOpenApi", "generateTypeScriptOpenApi")
    description = "Generates both Kotlin and TypeScript code from OpenAPI spec"
    group = "openapi"
}
