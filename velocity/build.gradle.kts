dependencies {
    compileOnlyApi(projects.viaversionCommon)
    compileOnly(libs.velocity) {
        // Requires Java 17
        exclude("com.velocitypowered", "velocity-brigadier")
    }
    annotationProcessor(libs.velocity)
}

publishShadowJar()
