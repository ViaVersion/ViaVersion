rootProject.name = "viaversion-parent"

setupViaSubproject("common")
setupViaSubproject("bukkit")
setupViaSubproject("bukkit-legacy")
setupViaSubproject("bungee")
setupViaSubproject("velocity")
setupViaSubproject("sponge")
setupViaSubproject("sponge-legacy")
setupViaSubproject("fabric")
setupViaSubproject("universal")

fun setupViaSubproject(name: String) {
    setupSubproject("viaversion-$name") {
        projectDir = file(name)
    }
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
