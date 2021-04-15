import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

tasks {
    withType<ShadowJar> {
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
            val shadowJarTask = subproject.tasks.getByName("shadowJar", ShadowJar::class)
            dependsOn(shadowJarTask)
            dependsOn(subproject.tasks.withType<Jar>())
            from(zipTree(shadowJarTask.archiveFile))
        }
    }
    build {
        dependsOn(withType<ShadowJar>())
    }
}
