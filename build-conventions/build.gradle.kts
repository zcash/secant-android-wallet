import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    `kotlin-dsl`
}

dependencies {
    val rootProperties = getRootProperties()

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${rootProperties.getProperty("KOTLIN_VERSION")}")
    implementation("com.android.tools.build:gradle:${rootProperties.getProperty("ANDROID_GRADLE_PLUGIN_VERSION")}")
}

// A slightly gross way to use the root gradle.properties as the single source of truth for version numbers
fun getRootProperties() = loadProperties(File(project.projectDir.parentFile, "gradle.properties").path)
