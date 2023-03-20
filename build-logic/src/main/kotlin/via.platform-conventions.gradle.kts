import java.util.*

plugins {
    id("via.shadow-conventions")
}

tasks {
    shadowJar {
        archiveFileName.set("ViaVersion-${project.name.substringAfter("viaversion-").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}-${project.version}.jar")
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }
}
