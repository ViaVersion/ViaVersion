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
}

java {
    withJavadocJar()
}
