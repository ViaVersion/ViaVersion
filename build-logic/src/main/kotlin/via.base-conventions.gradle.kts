plugins {
    `java-library`
    `maven-publish`
}

tasks {
    // Variable replacements
    processResources {
        filesMatching(listOf("plugin.yml", "META-INF/sponge_plugins.json", "fabric.mod.json", "bungee.yml")) {
            expand("version" to project.version, "description" to project.description)
        }
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.addAll(listOf("-nowarn", "-Xlint:-unchecked", "-Xlint:-deprecation"))
    }
    test {
        useJUnitPlatform()
    }
}

java {
    javaTarget(8)
    withSourcesJar()
}

publishing {
    publications.create<MavenPublication>("mavenJava") {
        groupId = rootProject.group as String
        artifactId = project.name
        version = rootProject.version as String
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
