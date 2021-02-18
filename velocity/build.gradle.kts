dependencies {
    implementation(project(":viaversion-common"))
    compileOnly("com.velocitypowered", "velocity-api", Versions.velocityApi)
    annotationProcessor("com.velocitypowered", "velocity-api", Versions.velocityApi)
}
