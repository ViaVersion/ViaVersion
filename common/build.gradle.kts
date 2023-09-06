dependencies {
    api(projects.viaversionApi)
    api(projects.viaversionApiLegacy)
    implementation(projects.compat.snakeyaml2Compat)
    implementation(projects.compat.snakeyaml1Compat)
}

java {
    withJavadocJar()
}
