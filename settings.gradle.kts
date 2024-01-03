enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    // configures repositories for all projects
    repositories {
        maven("https://repo.viaversion.com")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://libraries.minecraft.net")
        mavenCentral()
    }
    // only use these repos
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    // default plugin versions
    plugins {
        id("net.kyori.blossom") version "2.1.0"
        id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
        id("com.github.johnrengelman.shadow") version "8.1.1"
    }
}

rootProject.name = "viaversion-parent"

includeBuild("build-logic")

include("compat", "compat:snakeyaml-compat-common", "compat:snakeyaml2-compat", "compat:snakeyaml1-compat", "compat:protocolsupport-compat")

setupViaSubproject("api")
setupViaSubproject("api-legacy")
setupViaSubproject("common")
setupViaSubproject("bukkit")
setupViaSubproject("bukkit-legacy")
setupViaSubproject("bungee")
setupViaSubproject("velocity")
setupViaSubproject("sponge")
setupViaSubproject("fabric")
setupViaSubproject("template")

setupSubproject("viaversion") {
    projectDir = file("universal")
}

fun setupViaSubproject(name: String) {
    setupSubproject("viaversion-$name") {
        projectDir = file(name)
    }
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
