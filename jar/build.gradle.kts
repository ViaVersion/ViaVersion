import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation(project(":viaversion-common"))
    implementation(project(":viaversion-bukkit"))
    implementation(project(":viaversion-bungee"))
    implementation(project(":viaversion-fabric"))
    implementation(project(":viaversion-sponge"))
    implementation(project(":viaversion-velocity"))
}

tasks {
    withType<ShadowJar>() {
        archiveFileName.set("ViaVersion-" + project.version + ".jar")
    }
}

description = "viaversion-jar"
