plugins {
    id("net.kyori.blossom")
    id("via.shadow-conventions")
}

blossom {
    replaceToken("\$VERSION", project.version)
    replaceToken("\$IMPL_VERSION", "git-ViaVersion-${project.version}:${rootProject.latestCommitHash()}")
}

dependencies {
    api(projects.adventure) {
        targetConfiguration = "shadow"
    }
    api(libs.fastutil)
    api(libs.openNBT)
    api(libs.gson)

    compileOnlyApi(libs.snakeYaml)
    compileOnlyApi(libs.netty)
    compileOnlyApi(libs.guava)
    compileOnlyApi(libs.checkerQual)
}

java {
    withJavadocJar()
}
