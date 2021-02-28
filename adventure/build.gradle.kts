import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin

// Shade and relocate adventure in an extra module, so that common/the rest can directly depend on a
// relocated adventure without breaking native platform's adventure usage with project wide relocation
apply<ShadowPlugin>()
tasks {
    withType<ShadowJar> {
        relocate("net.kyori", "us.myles.viaversion.libs.kyori")
    }
    getByName("build") {
        dependsOn(withType<ShadowJar>())
    }
}

dependencies {
    api("net.kyori", "adventure-api", Versions.adventure) {
        exclude("org.checkerframework")
    }
    api("net.kyori", "adventure-text-serializer-gson", Versions.adventure) {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-bom")
        exclude("com.google.code.gson", "gson")
    }
    api("net.kyori", "adventure-text-serializer-legacy", Versions.adventure) {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-bom")
    }
}