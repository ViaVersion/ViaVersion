dependencies {
    implementation(project(":viaversion-bukkit-legacy"))
    implementation(project(":viaversion-common"))
    implementation("org.javassist", "javassist", Versions.javassist)
    compileOnly("org.spigotmc", "spigot-api", Versions.spigot) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }
}
