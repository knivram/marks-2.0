plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Using 6.6.0 to avoid Jackson 2.17+ which has Java 21 classes incompatible with Gradle 7.5.1's ASM
    implementation("org.openapitools:openapi-generator-gradle-plugin:6.6.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
}

gradlePlugin {
    plugins {
        create("marks-openapi") {
            id = "ch.ergon.marks.openapi"
            implementationClass = "ch.ergon.marks.openapi.OpenApiPlugin"
        }
    }
}
