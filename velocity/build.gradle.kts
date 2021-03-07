dependencies {
    implementation(project(":viaversion-common"))
    compileOnly("com.velocitypowered", "velocity-api", Versions.velocity)
    annotationProcessor("com.velocitypowered", "velocity-api", Versions.velocity)
}
