dependencies {
    api(projects.viaversionApi)
    api(projects.viaversionApiLegacy)
    api(rootProject.libs.text) {
        exclude("com.google.code.gson", "gson")
    }
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

tasks.named<Jar>("sourcesJar") {
    from(project(":viaversion-api").sourceSets.main.get().allSource)
}

publishShadowJar()
