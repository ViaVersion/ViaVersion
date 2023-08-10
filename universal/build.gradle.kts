import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    id("com.github.johnrengelman.shadow")
    id("io.papermc.hangar-publish-plugin") version "0.0.5"
    id("com.modrinth.minotaur") version "2.+"
}

val platforms = setOf(
    rootProject.projects.viaversionBukkit,
    rootProject.projects.viaversionBungee,
    rootProject.projects.viaversionFabric,
    rootProject.projects.viaversionSponge,
    rootProject.projects.viaversionVelocity
).map { it.dependencyProject }

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("ViaVersion-${project.version}.jar")
        destinationDirectory.set(rootProject.projectDir.resolve("build/libs"))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        platforms.forEach { platform ->
            val shadowJarTask = platform.tasks.named<ShadowJar>("shadowJar").forUseAtConfigurationTime().get()
            dependsOn(shadowJarTask)
            dependsOn(platform.tasks.withType<Jar>())
            from(zipTree(shadowJarTask.archiveFile))
        }
    }
    build {
        dependsOn(shadowJar)
    }
    sourcesJar {
        rootProject.subprojects.forEach { subproject ->
            if (subproject == project) return@forEach
            val platformSourcesJarTask = subproject.tasks.findByName("sourcesJar") as? Jar ?: return@forEach
            dependsOn(platformSourcesJarTask)
            from(zipTree(platformSourcesJarTask.archiveFile))
        }
    }
}

publishShadowJar()

val branch = rootProject.branchName()
val ver = (project.version as String) + "+" + System.getenv("GITHUB_RUN_NUMBER")
val changelogContent = rootProject.lastCommitMessage()
modrinth {
    val mcVersions: List<String> = (property("mcVersions") as String)
            .split(",")
            .map { it.trim() }
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("viaversion")
    versionType.set(if (branch == "master") "beta" else "alpha")
    versionNumber.set(ver)
    versionName.set("[$branch] $ver")
    changelog.set(changelogContent)
    uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    gameVersions.set(mcVersions)
    loaders.add("fabric")
    autoAddDependsOn.set(false)
    detectLoaders.set(false)
    dependencies {
        optional.project("viafabric")
        optional.project("viafabricplus")
    }
}

if (branch == "master") { // Don't spam releases until Hangar has per channel notifications
    hangarPublish {
        publications.register("plugin") {
            version.set(ver)
            namespace("ViaVersion", "ViaVersion")
            channel.set(if (branch == "master") "Snapshot" else "Alpha")
            changelog.set(changelogContent)
            apiKey.set(System.getenv("HANGAR_TOKEN"))
            platforms {
                register(Platforms.PAPER) {
                    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                    platformVersions.set(listOf(property("mcVersionRange") as String))
                }
            }
        }
    }
}
