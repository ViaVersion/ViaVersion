import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion

fun Project.latestCommitHash(): Provider<String> {
    return runGitCommand(listOf("rev-parse", "--short", "HEAD"))
}

fun Project.latestCommitMessage(): Provider<String> {
    return runGitCommand(listOf("log", "-1", "--pretty=%B"))
}

fun Project.branchName(): Provider<String> {
    return runGitCommand(listOf("rev-parse", "--abbrev-ref", "HEAD"))
}

fun Project.runGitCommand(args: List<String>): Provider<String> {
    return providers.exec {
        commandLine = listOf("git") + args
    }.standardOutput.asText.map {
        it.trim()
    }
}

fun JavaPluginExtension.javaTarget(version: Int) {
    toolchain.languageVersion.set(JavaLanguageVersion.of(version))
}
