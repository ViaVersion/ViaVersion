dependencies {
    implementation(project(":viaversion-bukkit-legacy"))
    implementation(project(":viaversion-common"))
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }
}

description = "viaversion-bukkit"
