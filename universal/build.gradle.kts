import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    id("com.github.johnrengelman.shadow")
    id("io.papermc.hangar-publish-plugin") version "0.1.0"
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
val baseVersion = project.version as String
val isRelease = !baseVersion.contains('-')
val isMainBranch = branch == "master"
if (!isRelease || isMainBranch) { // Only publish releases from the main branch
    val suffixedVersion = if (isRelease) baseVersion else baseVersion + "+" + System.getenv("GITHUB_RUN_NUMBER")
    val changelogContent = if (isRelease) {
        "See [GitHub](https://github.com/ViaVersion/ViaVersion) for release notes."
    } else {
        val commitHash = rootProject.latestCommitHash()
        "[$commitHash](https://github.com/ViaVersion/ViaVersion/commit/$commitHash) ${rootProject.latestCommitMessage()}"
    }

    modrinth {
        val mcVersions: List<String> = (property("mcVersions") as String)
                .split(",")
                .map { it.trim() }
        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set("viaversion")
        versionType.set(if (isRelease) "release" else if (isMainBranch) "beta" else "alpha")
        versionNumber.set(suffixedVersion)
        versionName.set(suffixedVersion)
        changelog.set(changelogContent)
        uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
        gameVersions.set(mcVersions)
        loaders.add("fabric")
        loaders.add("paper")
        loaders.add("folia")
        loaders.add("velocity")
        loaders.add("bungeecord")
        loaders.add("sponge")
        autoAddDependsOn.set(false)
        detectLoaders.set(false)
        dependencies {
            optional.project("viafabric")
            optional.project("viafabricplus")
        }
    }

    hangarPublish {
        publications.register("plugin") {
            version.set(suffixedVersion)
            id.set("ViaVersion")
            channel.set(if (isRelease) "Release" else if (isMainBranch) "Snapshot" else "Alpha")
            changelog.set(changelogContent)
            apiKey.set(System.getenv("HANGAR_TOKEN"))
            platforms {
                register(Platforms.PAPER) {
                    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                    platformVersions.set(listOf(property("mcVersionRange") as String))
                }
                register(Platforms.VELOCITY) {
                    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                    platformVersions.set(listOf(property("velocityVersion") as String))
                }
                register(Platforms.WATERFALL) {
                    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                    platformVersions.set(listOf(property("waterfallVersion") as String))
                }
            }
        }
    }
}
