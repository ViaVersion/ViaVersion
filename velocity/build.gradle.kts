dependencies {
    implementation(projects.viaversionCommon)
    compileOnly(libs.velocity) {
        exclude("com.velocitypowered", "velocity-brigadier")
    }
    annotationProcessor(libs.velocity)
}
