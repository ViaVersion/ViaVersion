dependencies {
    implementation(project(":viaversion-bukkit-legacy"))
    implementation(project(":viaversion-common"))
    compileOnly("org.spigotmc", "spigot-api", Versions.spigot) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }
}
