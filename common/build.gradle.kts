dependencies {
    api(projects.viaversionApi)
    api(rootProject.libs.text) {
        exclude("com.google.code.gson", "gson")
        exclude("com.google.guava", "guava")
        exclude("com.viaversion", "nbt")
    }

    // Note: If manually starting tests doesn't work for you in IJ, change 'Gradle -> Run Tests Using' to 'IntelliJ IDEA'
    testImplementation(rootProject.libs.netty)
    testImplementation(rootProject.libs.guava)
    testImplementation(rootProject.libs.snakeYaml)
    testImplementation(rootProject.libs.bundles.junit)
}

java {
    withJavadocJar()
}

tasks.named<Jar>("sourcesJar") {
    from(project(":viaversion-api").sourceSets.main.get().allSource)
}

// Task to quickly test/debug code changes using https://github.com/ViaVersion/ViaProxy
// For further instructions see the ViaProxy repository README
tasks.register<JavaExec>("runViaProxy") {
    dependsOn(tasks.shadowJar)

    val viaProxyConfiguration = configurations.create("viaProxy")
    viaProxyConfiguration.dependencies.add(dependencies.create(rootProject.libs.viaProxy.get().copy().setTransitive(false)))

    mainClass.set("net.raphimc.viaproxy.ViaProxy")
    classpath = viaProxyConfiguration
    workingDir = file("run")
    jvmArgs = listOf("-DskipUpdateCheck")

    if (System.getProperty("viaproxy.gui.autoStart") != null) {
        jvmArgs("-Dviaproxy.gui.autoStart")
    }

    doFirst {
        val jarsDir = file("$workingDir/jars")
        jarsDir.mkdirs()
        file("$jarsDir/${project.name}.jar").writeBytes(tasks.shadowJar.get().archiveFile.get().asFile.readBytes())
    }

    doLast {
        file("$workingDir/jars/${project.name}.jar").delete()
        file("$workingDir/logs").deleteRecursively()
    }
}
