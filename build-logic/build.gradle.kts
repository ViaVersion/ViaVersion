plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    // version must be manually kept in sync with the one in root project settings.gradle.kts
    implementation("gradle.plugin.com.github.jengelman.gradle.plugins", "shadow", "7.0.0")
}
