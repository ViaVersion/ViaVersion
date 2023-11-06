dependencies {
    compileOnlyApi(projects.viaversionCommon)
    compileOnly(libs.velocity) {
        // Requires Java 11
        exclude("com.velocitypowered", "velocity-brigadier")
    }
    annotationProcessor(libs.velocity)
}
