plugins {
    id("net.kyori.blossom")
}

blossom {
    replaceToken("\$VERSION", project.version)
    replaceToken("\$IMPL_VERSION", "git-ViaVersion-${project.version}:${rootProject.latestCommitHash()}")
}

dependencies {
    api(projects.viaversionApi)
    api(projects.viaversionApiLegacy)
    implementation(projects.compat.snakeyaml2Compat)
    implementation(projects.compat.snakeyaml1Compat)
}

java {
    withJavadocJar()
}
