plugins {
    id("io.papermc.hangar-publish-plugin") version "0.1.4"
    id("com.modrinth.minotaur") version "2.+"

    // A nice no-conflict comment for patching in downgrading
}

dependencies {
    api(projects.viaversionCommon)
    api(projects.viaversionBukkit)
    api(projects.viaversionVelocity)
    api(projects.viaversionSponge)
    api(projects.viaversionFabric)
}

tasks {
    shadowJar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
        archiveClassifier.set("")
        archiveFileName.set("ViaVersion-${project.version}.jar")
        destinationDirectory.set(rootProject.projectDir.resolve("build/libs"))
    }
    sourcesJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        rootProject.subprojects.forEach { subproject ->
            if (subproject == project) return@forEach
            val platformSourcesJarTask = subproject.tasks.findByName("sourcesJar") as? Jar ?: return@forEach
            dependsOn(platformSourcesJarTask)
            from(zipTree(platformSourcesJarTask.archiveFile))
        }
    }
}

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
        autoAddDependsOn.set(false)
        detectLoaders.set(false)
        dependencies {
            optional.project("viafabric")
            optional.project("viabackwards")
            optional.project("viarewind")
        }
    }
    tasks.modrinth {
        notCompatibleWithConfigurationCache("")
    }

    hangarPublish {
        publications.register("plugin") {
            version.set(suffixedVersion)
            id.set("ViaVersion")
            channel.set(if (isRelease) "Release" else if (isMainBranch) "Snapshot" else "Alpha")
            changelog.set(changelogContent)
            apiKey.set(System.getenv("HANGAR_TOKEN"))
            platforms {
                paper {
                    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                    platformVersions.set(listOf(property("mcVersionRange") as String))
                    dependencies {
                        hangar("ViaBackwards") {
                            required = false
                        }
                        hangar("ViaRewind") {
                            required = false
                        }
                    }
                }
                velocity {
                    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                    platformVersions.set(listOf(property("velocityVersion") as String))
                    dependencies {
                        hangar("ViaBackwards") {
                            required = false
                        }
                        hangar("ViaRewind") {
                            required = false
                        }
                    }
                }
            }
        }
    }
    tasks.named("publishPluginPublicationToHangar") {
        notCompatibleWithConfigurationCache("")
    }
}
