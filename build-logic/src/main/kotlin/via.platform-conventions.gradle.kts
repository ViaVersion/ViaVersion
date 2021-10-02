plugins {
    id("via.shadow-conventions")
}

tasks {
    shadowJar {
        archiveFileName.set("ViaVersion-${project.name.substringAfter("viaversion-").capitalize()}-${project.version}.jar")
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }
}
