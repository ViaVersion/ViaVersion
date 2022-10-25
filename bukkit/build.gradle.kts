dependencies {
    implementation(projects.viaversionBukkitLegacy)
    implementation(projects.viaversionCommon)
    compileOnly(libs.paper) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }
    compileOnly(projects.compat.protocolsupportCompat)
}
