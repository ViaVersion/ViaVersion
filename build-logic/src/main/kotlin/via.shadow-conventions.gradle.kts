import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named

plugins {
    id("via.base-conventions")
    id("com.github.johnrengelman.shadow")
}

tasks {
    named<Jar>("jar") {
        archiveClassifier.set("unshaded")
        from(project.rootProject.file("LICENSE"))
    }
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        configureRelocations()
        configureExcludes()
    }
    named("build") {
        dependsOn(shadowJar)
    }
}

publishShadowJar()

fun ShadowJar.configureRelocations() {
    relocate("javassist", "com.viaversion.viaversion.libs.javassist")
    relocate("com.google.gson", "com.viaversion.viaversion.libs.gson")
    relocate("com.github.steveice10.opennbt", "com.viaversion.viaversion.libs.opennbt")
    relocate("it.unimi.dsi.fastutil", "com.viaversion.viaversion.libs.fastutil")
}

fun ShadowJar.configureExcludes() {
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
    exclude("it/unimi/dsi/fastutil/*/*Tree*")
    exclude("it/unimi/dsi/fastutil/*/*Heap*")
    exclude("it/unimi/dsi/fastutil/*/*Queue*")
    // Crossing fingers
    exclude("it/unimi/dsi/fastutil/*/*Big*")
    exclude("it/unimi/dsi/fastutil/*/*Synchronized*")
    exclude("it/unimi/dsi/fastutil/*/*Unmodifiable*")
    exclude("it/unimi/dsi/fastutil/io/*")
}
