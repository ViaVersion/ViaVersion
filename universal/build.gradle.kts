plugins {
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
    id("io.papermc.hangar-publish-plugin") version "0.1.4"

    // A nice no-conflict comment for patching in downgrading
}

dependencies {
    api(projects.viaversionCommon)
    api(projects.viaversionBukkit)
    api(projects.viaversionVelocity)
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
            val sourceSets = subproject.extensions.findByType(SourceSetContainer::class) ?: return@forEach
            from(sourceSets.named("main").map { it.allSource })
        }
    }
}

val runNumber: String? = System.getenv("GITHUB_RUN_NUMBER")
val branch = if (runNumber != null) rootProject.branchName() else ""
val baseVersion = project.version as String
val isRelease = !baseVersion.contains('-')
val isMainBranch = branch == "master"
if (runNumber != null && (!isRelease || isMainBranch)) { // Only publish releases from the main branch
    val suffixedVersion = if (isRelease) baseVersion else "$baseVersion+$runNumber"
    val changelogContent = if (isRelease) {
        "See [GitHub](https://github.com/ViaVersion/ViaVersion) for release notes."
    } else {
        val commitHash = rootProject.latestCommitHash()
        "[$commitHash](https://github.com/ViaVersion/ViaVersion/commit/$commitHash) ${rootProject.latestCommitMessage()}"
    }

    val mcVersionRange = property("mcVersionRange") as String
    val (mcVersionStart, mcVersionEnd) = mcVersionRange.split("-", limit = 2)
    publishMods {
        file.set(tasks.shadowJar.flatMap { it.archiveFile })
        type.set(if (isRelease) STABLE else if (isMainBranch) BETA else ALPHA)
        version.set(suffixedVersion)
        displayName.set(suffixedVersion)
        changelog.set(changelogContent)

        val modrinthOptions = modrinthOptions {
            accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
            projectId.set("P1OZGk5p")
            optional("viafabric", "viabackwards", "viarewind")
        }
        modrinth {
            from(modrinthOptions)
            modLoaders.addAll("paper", "folia", "velocity")
            minecraftVersionRange {
                start.set(mcVersionStart)
                end.set(mcVersionEnd)
            }
        }
        // Fabric requires 1.14+ (18w43b), so it is uploaded separately with only the supported versions
        modrinth("modrinthFabric") {
            from(modrinthOptions)
            modLoaders.add("fabric")
            minecraftVersionRange {
                start.set("1.14")
                end.set(mcVersionEnd)
            }
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
                paper {
                    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                    platformVersions.set(listOf(mcVersionRange))
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
}
