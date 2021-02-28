import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import java.io.ByteArrayOutputStream

fun Project.configureShadowJar() {
    apply<ShadowPlugin>()
    tasks {
        withType<ShadowJar> {
            archiveClassifier.set("")
            archiveFileName.set("ViaVersion-${project.name.substringAfter("viaversion-").capitalize()}-${project.version}.jar")
            destinationDirectory.set(rootProject.projectDir.resolve("build/libs"))
            configureRelocations()
            configureExcludes()
        }
        getByName("build") {
            dependsOn(withType<ShadowJar>())
        }
        withType<Jar> {
            if (name == "jar") {
                archiveClassifier.set("unshaded")
            }
        }
    }
}

private fun ShadowJar.configureRelocations() {
    relocate("javassist", "us.myles.viaversion.libs.javassist")
    relocate("com.google.gson", "us.myles.viaversion.libs.gson")
    relocate("com.github.steveice10.opennbt", "us.myles.viaversion.libs.opennbt")
    relocate("it.unimi.dsi.fastutil", "us.myles.viaversion.libs.fastutil")
}

private fun ShadowJar.configureExcludes() {
    // FastUtil - we only want object and int maps
    // Object types
    exclude("it/unimi/dsi/fastutil/*/*Reference*")
    exclude("it/unimi/dsi/fastutil/*/*Boolean*")
    exclude("it/unimi/dsi/fastutil/*/*Byte*")
    exclude("it/unimi/dsi/fastutil/*/*Short*")
    exclude("it/unimi/dsi/fastutil/*/*Float*")
    exclude("it/unimi/dsi/fastutil/*/*Double*")
    exclude("it/unimi/dsi/fastutil/*/*Long*")
    exclude("it/unimi/dsi/fastutil/*/*Char*")
    // Map types
    exclude("it/unimi/dsi/fastutil/*/*Custom*")
    exclude("it/unimi/dsi/fastutil/*/*Linked*")
    exclude("it/unimi/dsi/fastutil/*/*Sorted*")
    exclude("it/unimi/dsi/fastutil/*/*Tree*")
    exclude("it/unimi/dsi/fastutil/*/*Heap*")
    exclude("it/unimi/dsi/fastutil/*/*Queue*")
    // Crossing fingers
    exclude("it/unimi/dsi/fastutil/*/*Big*")
    exclude("it/unimi/dsi/fastutil/*/*Synchronized*")
    exclude("it/unimi/dsi/fastutil/*/*Unmodifiable*")
    exclude("it/unimi/dsi/fastutil/io/*")
}

fun Project.latestCommitHash(): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "rev-parse", "--short", "HEAD")
        standardOutput = byteOut
    }
    return byteOut.toString(Charsets.UTF_8.name()).trim()
}
