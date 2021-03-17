import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin

plugins {
    `java-library`
    `maven-publish`
}

allprojects {
    group = "us.myles"
    version = "3.3.0-21w11a"
    description = "Allow newer clients to join older server versions."
}

subprojects {
    apply<JavaLibraryPlugin>()
    apply<MavenPublishPlugin>()

    tasks {
        // Variable replacements
        processResources {
            filesMatching(listOf("plugin.yml", "mcmod.info", "fabric.mod.json", "bungee.yml")) {
                expand("version" to project.version, "description" to project.description)
            }
        }
        withType<Javadoc> {
            options.encoding = Charsets.UTF_8.name()
        }
        withType<JavaCompile> {
            options.encoding = Charsets.UTF_8.name()
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
    if (project.name == "viaversion") {
        apply<ShadowPlugin>()
    }

    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
        maven("https://nexus.velocitypowered.com/repository/velocity-artifacts-snapshots/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
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

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        withSourcesJar()
        withJavadocJar()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = rootProject.group as String
                artifactId = project.name
                version = rootProject.version as String

                if (plugins.hasPlugin(ShadowPlugin::class.java)) {
                    artifact(tasks["shadowJar"])
                } else {
                    from(components["java"])
                }
            }
        }
        repositories.maven {
            name = "Via"
            url = uri("https://repo.viaversion.com/")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

tasks {
    // root project has no useful artifacts
    withType<Jar> {
        onlyIf { false }
    }
}
