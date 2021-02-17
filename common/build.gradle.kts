plugins {
    id("net.kyori.blossom") version "1.1.0"
    id("org.ajoberstar.grgit") version "4.1.0"
}

val commitId: String = grgit.head().abbreviatedId

blossom {
    replaceToken("\$VERSION", project.version)
    replaceToken("\$IMPL_VERSION", "git-ViaVersion-" + project.version + ":" + commitId)
}

dependencies {
    implementation("net.md-5:bungeecord-chat:1.16-R0.5-SNAPSHOT")
    implementation("it.unimi.dsi:fastutil:8.3.1")
    implementation("com.github.steveice10:opennbt:1.2-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.javassist:javassist:3.27.0-GA")
    implementation("org.yaml:snakeyaml:1.18")

    compileOnly("io.netty:netty-all:4.0.20.Final")
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("org.jetbrains:annotations:19.0.0")

    testImplementation("io.netty:netty-all:4.0.20.Final")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.3")
}

description = "viaversion-common"
