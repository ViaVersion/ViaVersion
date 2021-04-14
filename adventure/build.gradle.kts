import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow")
}

// Shade and relocate adventure in an extra module, so that common/the rest can directly depend on a
// relocated adventure without breaking native platform's adventure usage with project wide relocation
tasks {
    withType<ShadowJar> {
        relocate("net.kyori", "us.myles.viaversion.libs.kyori")
    }
    getByName("build") {
        dependsOn(withType<ShadowJar>())
    }
}

dependencies {
    api(libs.bundles.adventure) {
        exclude("org.checkerframework")
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-bom")
        exclude("com.google.code.gson", "gson")
    }
}