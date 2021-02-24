import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        archiveFileName.set("ViaVersion-${project.version}.jar")
        destinationDirectory.set(rootProject.projectDir.resolve("build/libs"))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        arrayOf(
            "bukkit",
            "bungee",
            "fabric",
            "sponge",
            "velocity"
        ).forEach {
            val subProject = rootProject.project(":viaversion-$it")
            val shadowJarTask = subProject.tasks.getByName("shadowJar")
            from(zipTree(shadowJarTask.outputs.files.singleFile))
            dependsOn(shadowJarTask)
        }
    }
    build {
        dependsOn(withType<ShadowJar>())
    }
}
