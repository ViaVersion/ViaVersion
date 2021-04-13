plugins {
    id("net.kyori.blossom") version "1.2.0"
}

blossom {
    replaceToken("\$VERSION", project.version)
    replaceToken("\$IMPL_VERSION", "git-ViaVersion-${project.version}:${rootProject.latestCommitHash()}")
}

dependencies {
    api(project(":viaversion-api"))
}
