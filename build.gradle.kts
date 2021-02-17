import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version ("6.1.0") apply false
}

subprojects {
    apply {
        plugin<JavaPlugin>()
        plugin<JavaLibraryPlugin>()
        plugin<MavenPublishPlugin>()
        plugin<ShadowPlugin>()
    }

    group = "us.myles"
    version = "3.3.0-21w06a"
    description = "Allow newer clients to join older server versions."

    java.sourceCompatibility = JavaVersion.VERSION_1_8

    tasks {
        build {
            dependsOn(withType<ShadowJar>())
        }

        test {
            useJUnitPlatform()
        }

        withType<JavaCompile>() {
            options.encoding = "UTF-8"
        }
        javadoc {
            options.encoding = "UTF-8"
        }

        // Variable replacements
        processResources {
            filesMatching(listOf("plugin.yml", "mcmod.info", "fabric.mod.json", "bungee.yml")) {
                expand("version" to project.version, "description" to project.description)
            }
        }

        withType<ShadowJar>() {
            archiveFileName.set("ViaVersion-" + project.version + ".jar")

            relocate("org.yaml.snakeyaml", "us.myles.viaversion.libs.snakeyaml")
            relocate("javassist", "us.myles.viaversion.libs.javassist")
            relocate("com.google.gson", "us.myles.viaversion.libs.gson")
            relocate("com.github.steveice10.opennbt", "us.myles.viaversion.libs.opennbt")

            relocate("net.md_5.bungee", "us.myles.viaversion.libs.bungeecordchat") {
                include("net.md_5.bungee.api.chat.*")
                include("net.md_5.bungee.api.ChatColor")
                include("net.md_5.bungee.api.ChatMessageType")
                include("net.md_5.bungee.chat.*")
            }

            relocate("it.unimi.dsi.fastutil", "us.myles.viaversion.libs.fastutil") {
                // We only want int and Object maps
                include("it.unimi.dsi.fastutil.ints.*")
                include("it.unimi.dsi.fastutil.objects.*")
                include("it.unimi.dsi.fastutil.*.class")
                // Object types
                exclude("it.unimi.dsi.fastutil.*.*Reference*")
                exclude("it.unimi.dsi.fastutil.*.*Boolean*")
                exclude("it.unimi.dsi.fastutil.*.*Byte*")
                exclude("it.unimi.dsi.fastutil.*.*Short*")
                exclude("it.unimi.dsi.fastutil.*.*Float*")
                exclude("it.unimi.dsi.fastutil.*.*Double*")
                exclude("it.unimi.dsi.fastutil.*.*Long*")
                exclude("it.unimi.dsi.fastutil.*.*Char*")
                // Map types
                exclude("it.unimi.dsi.fastutil.*.*Custom*")
                exclude("it.unimi.dsi.fastutil.*.*Linked*")
                exclude("it.unimi.dsi.fastutil.*.*Sorted*")
                exclude("it.unimi.dsi.fastutil.*.*Tree*")
                exclude("it.unimi.dsi.fastutil.*.*Heap*")
                exclude("it.unimi.dsi.fastutil.*.*Queue*")
                // Crossing fingers
                exclude("it.unimi.dsi.fastutil.*.*Big*")
                exclude("it.unimi.dsi.fastutil.*.*Synchronized*")
                exclude("it.unimi.dsi.fastutil.*.*Unmodifiable*")
            }
        }
    }

    repositories {
        maven {
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
        }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
        maven {
            url = uri("https://nexus.velocitypowered.com/repository/velocity-artifacts-snapshots/")
        }
        maven {
            url = uri("https://repo.spongepowered.org/maven")
        }
        maven {
            url = uri("https://repo.viaversion.com")
        }
        maven {
            url = uri("https://libraries.minecraft.net")
        }
        maven {
            url = uri("https://repo.maven.apache.org/maven2/")
        }
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

    java {
        withSourcesJar()
        withJavadocJar()
    }

    publishing {
        publications.create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}