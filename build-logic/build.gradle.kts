plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    // version must be manually kept in sync with the one in root project settings.gradle.kts
    implementation("com.github.johnrengelman", "shadow", "8.1.1")
}
