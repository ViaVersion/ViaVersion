enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    // configures repositories for all projects
    repositories {
        maven("https://repo.viaversion.com")
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenCentral()
    }
    // only use these repos
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    // default plugin versions
    plugins {
        id("com.gradleup.shadow") version "9.1.0"
        id("net.kyori.blossom") version "2.1.0"
        id("org.jetbrains.gradle.plugin.idea-ext") version "1.2"

        // A nice no-conflict comment for patching in downgrading
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "viaversion-parent"

includeBuild("build-logic")

setupViaSubproject("api")
setupViaSubproject("common")
setupViaSubproject("bukkit")
setupViaSubproject("bukkit-legacy")
setupViaSubproject("velocity")
setupViaSubproject("sponge")
setupViaSubproject("fabric")

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
