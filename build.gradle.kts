import net.kyori.indra.IndraPlugin
import net.kyori.indra.IndraPublishingPlugin
import net.kyori.indra.sonatype.IndraSonatypePublishingPlugin
import net.kyori.indra.sonatypeSnapshots

plugins {
    `java-library`
    `maven-publish`
    id("net.kyori.indra")
    id("net.kyori.indra.publishing.sonatype") version("1.3.1") apply false
}

group = "us.myles"
version = "3.3.0-21w07a"
description = "Allow newer clients to join older server versions."

subprojects {
    apply<JavaLibraryPlugin>()
    apply<MavenPublishPlugin>()
    apply<IndraPlugin>()
    apply<IndraPublishingPlugin>()
    apply<IndraSonatypePublishingPlugin>()

    tasks {
        // Variable replacements
        processResources {
            filesMatching(listOf("plugin.yml", "mcmod.info", "fabric.mod.json", "bungee.yml")) {
                expand("version" to project.version, "description" to project.description)
            }
        }
        withType<JavaCompile> {
            options.compilerArgs.addAll(listOf("-nowarn", "-Xlint:-unchecked", "-Xlint:-deprecation"))
        }
    }

    val platforms = listOf(
        "bukkit",
        "bungee",
        "fabric",
        "sponge",
        "velocity"
    ).map { "viaversion-$it" }
    if (platforms.contains(project.name)) {
        configureShadowJar()
    }

    repositories {
        mavenLocal()
        sonatypeSnapshots()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
        maven("https://nexus.velocitypowered.com/repository/velocity-artifacts-snapshots/")
        maven("https://repo.spongepowered.org/maven")
        maven("https://repo.viaversion.com")
        maven("https://libraries.minecraft.net")
        maven("https://repo.maven.apache.org/maven2/")
    }

    dependencies {
        testImplementation("io.netty", "netty-all", Versions.netty)
        testImplementation("com.google.guava", "guava", Versions.guava)
        testImplementation("org.junit.jupiter", "junit-jupiter-api", Versions.jUnit)
        testImplementation("org.junit.jupiter", "junit-jupiter-engine", Versions.jUnit)
    }

    indra {
        javaVersions {
            target.set(8)
            testWith(8, 11, 15)
        }
        github("ViaVersion", "ViaVersion") {
            issues = true
        }
        mitLicense()
    }

    //TODO use indra with proper repository (currently local)?
    /*extensions.configure<de.marcphilipp.gradle.nexus.NexusPublishExtension> {
        repositories.create("via") {
            snapshotRepositoryUrl.set(uri("https://repo.viaversion.com"))
            username.set(System.getenv("PUBLISHING_USERNAME"))
            password.set(System.getenv("PUBLISHING_PASSWORD"))
        }
    }*/
}

tasks {
    // root project has no useful artifacts
    withType<Jar> {
        onlyIf { false }
    }
}
