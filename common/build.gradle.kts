dependencies {
    api(projects.viaversionApi)
    api(rootProject.libs.text) {
        exclude("com.google.code.gson", "gson")
        exclude("com.viaversion", "nbt")
    }

    // Note: If manually starting tests doesn't work for you in IJ, change 'Gradle -> Run Tests Using' to 'IntelliJ IDEA'
    testImplementation(rootProject.libs.netty)
    testImplementation(rootProject.libs.guava)
    testImplementation(rootProject.libs.snakeYaml)
    testImplementation(rootProject.libs.bundles.junit)
    testRuntimeOnly(rootProject.libs.platformLauncher)
}

java {
    withJavadocJar()
}

tasks.named<Jar>("sourcesJar") {
    from(project(":viaversion-api").sourceSets.main.get().allSource)
}

// Task to quickly test/debug code changes using https://github.com/ViaVersion/ViaProxy
// For further instructions see the ViaProxy repository README
val prepareViaProxyFiles by tasks.registering(Copy::class) {
    dependsOn(project.tasks.shadowJar)

    from(project.tasks.shadowJar.map { it.archiveFile.get().asFile })
    into(layout.projectDirectory.dir("run/jars"))

    val projectName = project.name
    rename { "${projectName}.jar" }
}

val cleanupViaProxyFiles by tasks.registering(Delete::class) {
    delete(
        layout.projectDirectory.file("run/jars/${project.name}.jar"),
        layout.projectDirectory.dir("run/logs")
    )
}

val viaProxyConfiguration: Configuration by configurations.creating {
    dependencies.add(rootProject.libs.viaProxy.get().copy().setTransitive(false))
}

tasks.register<JavaExec>("runViaProxy") {
    dependsOn(prepareViaProxyFiles)
    finalizedBy(cleanupViaProxyFiles)

    mainClass.set("net.raphimc.viaproxy.ViaProxy")
    classpath = viaProxyConfiguration
    workingDir = layout.projectDirectory.dir("run").asFile
    jvmArgs = listOf("-DskipUpdateCheck")

    if (System.getProperty("viaproxy.gui.autoStart") != null) {
        jvmArgs("-Dviaproxy.gui.autoStart")
    }
}
