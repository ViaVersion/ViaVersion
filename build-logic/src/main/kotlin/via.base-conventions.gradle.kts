plugins {
    `java-library`
}

tasks {
    // Variable replacements
    processResources {
        filesMatching(listOf("plugin.yml", "META-INF/sponge_plugins.json", "fabric.mod.json")) {
            expand("version" to project.version, "description" to project.description, "url" to "https://viaversion.com")
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
    javaTarget(17)
    withSourcesJar()
}
