dependencies {
    implementation(project(":viaversion-common"))
    compileOnly("org.bukkit", "bukkit", Versions.legacyBukkit) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }
}
