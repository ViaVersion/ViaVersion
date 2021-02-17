plugins {
    id("net.kyori.blossom") version "1.1.0"
    id("org.ajoberstar.grgit") version "4.1.0"
}

val commitId: String = grgit.head().abbreviatedId

blossom {
    replaceToken("\$VERSION", project.version)
    replaceToken("\$IMPL_VERSION", "git-ViaVersion-" + project.version + ":" + commitId)
}

description = "viaversion-common"
