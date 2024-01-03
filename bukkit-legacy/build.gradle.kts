dependencies {
    compileOnlyApi(projects.viaversionCommon)
    compileOnly(libs.legacyBukkit) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }
}
