plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("net.kyori", "indra-common", "1.3.1")
    implementation("net.kyori", "indra-publishing-gradle-plugin", "1.3.1")
    implementation("com.github.jengelman.gradle.plugins", "shadow", "6.1.0")
}