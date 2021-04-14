dependencies {
    implementation(projects.viaversionCommon)
    implementation(projects.javaCompat)
    compileOnly(libs.bungee)
}

configure<JavaPluginConvention> {
    // This is necessary to allow compilation for Java 8 while still including
    // newer Java versions in the code.
    disableAutoTargetJvm()
}
