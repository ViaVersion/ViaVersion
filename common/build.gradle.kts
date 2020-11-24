plugins {
    id("net.kyori.blossom") version "1.1.0"
}

blossom {
    replaceToken("\$VERSION", project.version)
    replaceToken("\$IMPL_VERSION", "git-ViaVersion-${project.version}:${rootProject.latestCommitHash()}")
}

dependencies {
    api("it.unimi.dsi", "fastutil", Versions.fastUtil)
    api("com.github.steveice10", "opennbt", Versions.openNBT)
    api("com.google.code.gson", "gson", Versions.gson)

    api("net.kyori", "adventure-api", Versions.adventure) {
        exclude("org.checkerframework")
    }
    api("net.kyori", "adventure-text-serializer-gson", Versions.adventure) {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-bom")
        exclude("com.google.code.gson", "gson")
    }
    api("net.kyori", "adventure-text-serializer-legacy", Versions.adventure) {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-bom")
    }

    compileOnlyApi("org.yaml", "snakeyaml", Versions.snakeYaml)
    compileOnlyApi("io.netty", "netty-all", Versions.netty)
    compileOnlyApi("com.google.guava", "guava", Versions.guava)
    compileOnlyApi("org.jetbrains", "annotations", Versions.jetbrainsAnnotations)
}
