package ch.ergon.marks.openapi

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

interface OpenApiExtension {
    val typeScriptOutputDir: Property<String>
}

@Suppress("unused")
class OpenApiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(OpenApiGeneratorPlugin::class.java)

        // Create extension for configuration
        val extension = project.extensions.create("openApiConfig", OpenApiExtension::class.java)
        extension.typeScriptOutputDir.convention(
            project.layout.buildDirectory.dir("generated/typescript-axios").map { it.asFile.absolutePath }
        )

        val typeScriptOpenApiTask = project.tasks.register("generateTypeScriptOpenApi", GenerateTask::class.java)
        typeScriptOpenApiTask.configure(object : Action<GenerateTask> {
            override fun execute(task: GenerateTask) {
                configureDefaults(task, project)
                configureTs(task, project, extension)
            }
        })

        val kotlinOpenApiTask = project.tasks.register("generateKotlinOpenApi", GenerateTask::class.java)
        kotlinOpenApiTask.configure(object : Action<GenerateTask> {
            override fun execute(task: GenerateTask) {
                configureDefaults(task, project)
                configureKotlin(task, project)
            }
        })

        // Register generated sources for Kotlin compilation
        val kotlinGeneratedSourcesDir = kotlinOpenApiTask.map { project.file(it.outputDir.get()).resolve("src/main/kotlin") }

        project.afterEvaluate(object : Action<Project> {
            override fun execute(p: Project) {
                // Add generated sources to main source set
                p.extensions.findByName("kotlin")?.let { kotlinExt ->
                    val sourceSets = kotlinExt.javaClass.getMethod("getSourceSets").invoke(kotlinExt)
                    val mainSourceSet = sourceSets.javaClass.getMethod("getByName", String::class.java).invoke(sourceSets, "main")
                    val kotlin = mainSourceSet.javaClass.getMethod("getKotlin").invoke(mainSourceSet)
                    kotlin.javaClass.getMethod("srcDir", Any::class.java).invoke(kotlin, kotlinGeneratedSourcesDir)
                }
            }
        })
    }

    private fun configureDefaults(task: GenerateTask, project: Project) {
        task.inputSpec.set(project.layout.projectDirectory.file("src/main/resources/api.yaml").asFile.absolutePath)
        task.inputs.dir(project.layout.projectDirectory.dir("src/main/resources/"))

        task.releaseNote.set("Minor update")
        task.modelNamePrefix.set("")
        task.modelNameSuffix.set("")
        task.apiNameSuffix.set("")

        task.generateModelTests.set(false)
        task.generateModelDocumentation.set(false)
        task.generateApiTests.set(false)
        task.generateApiDocumentation.set(false)

        task.configOptions.set(mapOf<String, String>())
        task.logToStderr.set(false)
        task.enablePostProcessFile.set(false)
        task.skipValidateSpec.set(true)
        task.generateAliasAsModel.set(false)
    }

    private fun configureKotlin(task: GenerateTask, project: Project) {
        task.generatorName.set("kotlin-spring")
        task.apiPackage.set("api")
        task.modelPackage.set("dto")
        task.modelNameSuffix.set("Dto")
        task.configOptions.set(
            mapOf(
                "useSpringBoot3" to "true",
                "useTags" to "true",
                "skipDefaultInterface" to "true",
                "interfaceOnly" to "true",
                "useBeanValidation" to "false",
                "enumPropertyNaming" to "UPPERCASE",
                "documentationProvider" to "none",
                "annotationLibrary" to "none",
            ),
        )
        task.typeMappings.set(mapOf("java.time.OffsetDateTime" to "java.time.Instant"))
        task.outputDir.set(project.layout.buildDirectory.dir("generated/kotlin-spring").map { it.asFile.absolutePath })

        // Make template dir optional - use defaults if not present
        val templatePath = project.rootProject.layout.projectDirectory.dir("config/openapi/templates/kotlin-spring")
        if (templatePath.asFile.exists()) {
            task.templateDir.set(templatePath.asFile.absolutePath)
        }
    }

    private fun configureTs(task: GenerateTask, project: Project, extension: OpenApiExtension) {
        task.generatorName.set("typescript-axios")
        task.apiPackage.set("api")
        task.modelPackage.set("dto")
        task.modelNameSuffix.set("Dto")
        task.configOptions.set(
            mapOf(
                "supportsES6" to "true",
                "withSeparateModelsAndApi" to "true",
                "modelPropertyNaming" to "camelCase",
            ),
        )

        // Use configurable output directory
        task.outputDir.set(extension.typeScriptOutputDir)

        // Make template dir optional - use defaults if not present
        val templatePath = project.rootProject.layout.projectDirectory.dir("config/openapi/templates/typescript-axios")
        if (templatePath.asFile.exists()) {
            task.templateDir.set(templatePath.asFile.absolutePath)
        }
    }
}
