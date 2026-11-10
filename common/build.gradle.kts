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
val prepareViaProxyFiles = tasks.register<Copy>("prepareViaProxyFiles") {
    description = "Prepares the ViaProxy run dir for the runViaProxy task"
    dependsOn(project.tasks.shadowJar)

    from(project.tasks.shadowJar.map { it.archiveFile.get().asFile })
    into(layout.projectDirectory.dir("run/jars"))

    val projectName = project.name
    rename { "${projectName}.jar" }
}

val cleanupViaProxyFiles = tasks.register<Delete>("cleanupViaProxyFiles") {
    description = "Deletes ViaProxy logs and the compiled project jar from its jars dir"
    delete(
        layout.projectDirectory.file("run/jars/${project.name}.jar"),
        layout.projectDirectory.dir("run/logs")
    )
}

val viaProxyConfiguration: Configuration = configurations.create("viaProxyConfiguration") {
    dependencies.add(rootProject.libs.viaProxy.get().copy().setTransitive(false))
}

tasks.register<JavaExec>("runViaProxy") {
    description = "Runs ViaProxy locally"
    dependsOn(prepareViaProxyFiles)
    finalizedBy(cleanupViaProxyFiles)

    mainClass.set("net.raphimc.viaproxy.ViaProxy")
    classpath = viaProxyConfiguration
    workingDir = layout.projectDirectory.dir("run").asFile
    jvmArgs = listOf("-DskipUpdateCheck", "-Dviaproxy.gui.autoStart")

    if (System.getProperty("viaproxy.disableBackwardsPlatforms") != null) {
        jvmArgs("-Dviaproxy.enableViaBackwards=false", "-Dviaproxy.enableViaRewind=false")
    }
    if (System.getProperty("viaproxy.disableExtraPlatforms") != null) {
        jvmArgs("-Dviaproxy.enableViaBedrock=false", "-Dviaproxy.enableViaLegacy=false", "-Dviaproxy.enableViaAprilFools=false")
    }
}
