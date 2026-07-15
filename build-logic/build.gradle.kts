plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.4.2")

    // A nice no-conflict comment for patching in downgrading
}
