rootProject.name = "io.github.astridha.fix5decimal"
include(":library")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage") // See https://github.com/gradle/gradle/issues/32443
    repositories {
        google()
        mavenCentral()
    }
}

