dependencies {
    api(projects.viaversionApi)
    api(projects.viaversionApiLegacy)
    implementation(projects.compat.snakeyaml2Compat)
    implementation(projects.compat.snakeyaml1Compat)

    // Note: If manually starting tests doesn't work for you in IJ, change 'Gradle -> Run Tests Using' to 'IntelliJ IDEA'
    testImplementation(rootProject.libs.netty)
    testImplementation(rootProject.libs.guava)
    testImplementation(rootProject.libs.snakeYaml2)
    testImplementation(rootProject.libs.bundles.junit)
}

java {
    withJavadocJar()
}
