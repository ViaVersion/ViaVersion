import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("ViaVersion-${project.version}.jar")
        destinationDirectory.set(rootProject.projectDir.resolve("build/libs"))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        sequenceOf(
            rootProject.projects.viaversionBukkit,
            rootProject.projects.viaversionBungee,
            rootProject.projects.viaversionFabric,
            rootProject.projects.viaversionSponge,
            rootProject.projects.viaversionVelocity,
        ).map { it.dependencyProject }.forEach { subproject ->
            val shadowJarTask = subproject.tasks.named<ShadowJar>("shadowJar").forUseAtConfigurationTime().get()
            dependsOn(shadowJarTask)
            dependsOn(subproject.tasks.withType<Jar>())
            from(zipTree(shadowJarTask.archiveFile))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}

publishShadowJar()
