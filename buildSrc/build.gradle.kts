plugins {
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
    `kotlin-dsl`
    id("com.github.johnrengelman.shadow") version ("6.1.0") apply false
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
}

dependencies {
    compileOnly("com.github.jengelman.gradle.plugins", "shadow", "6.1.0")
}