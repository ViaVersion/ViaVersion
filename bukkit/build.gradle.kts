dependencies {
    implementation(project(":viaversion-bukkit-legacy"))
    implementation(project(":viaversion-common"))
    implementation("org.javassist", "javassist", Versions.javassist)
    compileOnly("com.destroystokyo.paper", "paper-api", Versions.paper) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }
}
