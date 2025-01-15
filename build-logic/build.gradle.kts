plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    // version must be manually kept in sync with the one in root project settings.gradle.kts
    implementation("com.gradleup.shadow", "shadow-gradle-plugin", "8.3.5")
}
