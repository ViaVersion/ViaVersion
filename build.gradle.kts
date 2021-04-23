import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin

plugins {
    `java-library`
    `maven-publish`
    id("net.kyori.blossom") version "1.2.0" apply false
}

allprojects {
    group = "us.myles"
    version = "4.0.0-21w16a"
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

    repositories {
        maven("https://repo.viaversion.com")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://nexus.velocitypowered.com/repository/velocity-artifacts-snapshots/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://libraries.minecraft.net")
        mavenCentral()
    }

    dependencies {
        // Note: If manually starting tests doesn't work for you in IJ, change 'Gradle -> Run Tests Using' to 'IntelliJ IDEA'
        testImplementation(rootProject.libs.netty)
        testImplementation(rootProject.libs.guava)
        testImplementation(rootProject.libs.bundles.junit)
    }

    configureJavaTarget(8)
    java {
        withSourcesJar()
        withJavadocJar()
    }
}

// Configure shadow tasks before the publishing task
sequenceOf(
        projects.viaversionBukkit,
        projects.viaversionBungee,
        projects.viaversionFabric,
        projects.viaversionSponge,
        projects.viaversionVelocity
).map { it.dependencyProject }.forEach { project ->
    project.configureShadowJar()
}

projects.viaversionApi.dependencyProject.configureShadowJarAPI()
projects.viaversion.dependencyProject.apply<ShadowPlugin>()

subprojects {
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
