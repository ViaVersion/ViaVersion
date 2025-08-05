plugins {
    `java-library`
    `jvm-test-suite`
}

tasks {
    // Variable replacements
    processResources {
        val ver = project.version.toString()
        val desc = project.description
        filesMatching(listOf("plugin.yml", "META-INF/sponge_plugins.json", "fabric.mod.json")) {
            expand(mapOf("version" to ver, "description" to desc))
        }
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.addAll(listOf("-nowarn", "-Xlint:-unchecked", "-Xlint:-deprecation"))
        options.isFork = true
    }
    test {
        useJUnitPlatform()
    }
}

java {
    javaTarget(17)
    withSourcesJar()
}
