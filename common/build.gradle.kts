plugins {
    id("net.kyori.blossom") version "1.1.0"
}

blossom {
    replaceToken("\$VERSION", project.version)
    replaceToken("\$IMPL_VERSION", "git-ViaVersion-${project.version}:${rootProject.latestCommitHash()}")
}

dependencies {
    api("net.md-5", "bungeecord-chat", Versions.bungeeChat) {
        exclude("com.google.guava")
    }
    api("it.unimi.dsi", "fastutil", Versions.fastUtil)
    api("com.github.steveice10", "opennbt", Versions.openNBT)
    api("com.google.code.gson", "gson", Versions.gson)
    api("org.javassist", "javassist", Versions.javassist)
    api("org.yaml", "snakeyaml", Versions.snakeYaml)

    compileOnlyApi("io.netty", "netty-all", Versions.netty)
    compileOnlyApi("com.google.guava", "guava", Versions.guava)
    compileOnlyApi("org.jetbrains", "annotations", Versions.jetbrainsAnnotations)
}
