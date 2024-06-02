import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import java.io.ByteArrayOutputStream

fun Project.publishShadowJar() {
    extensions.configure<PublishingExtension> {
        publications.named<MavenPublication>("mavenJava") {
            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
        }
    }
}

fun Project.latestCommitHash(): String {
    return runGitCommand(listOf("rev-parse", "--short", "HEAD"))
}

fun Project.latestCommitMessage(): String {
    return runGitCommand(listOf("log", "-1", "--pretty=%B"))
}

fun Project.branchName(): String {
    return runGitCommand(listOf("rev-parse", "--abbrev-ref", "HEAD"))
}

fun Project.runGitCommand(args: List<String>): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git") + args
        standardOutput = byteOut
    }
    return byteOut.toString(Charsets.UTF_8.name()).trim()
}

fun JavaPluginExtension.javaTarget(version: Int) {
    toolchain.languageVersion.set(JavaLanguageVersion.of(version))
}
