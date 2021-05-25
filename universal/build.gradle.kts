import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow")
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
