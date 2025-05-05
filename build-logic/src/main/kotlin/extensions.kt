import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

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
    return providers.of(GitCommand::class.java) { parameters.args.set(args) }.getOrNull() ?: "unknown"
}

abstract class GitCommand : ValueSource<String, GitCommand.GitCommandParameters> {

    @get:Inject
    abstract val execOperations: ExecOperations

    interface GitCommandParameters : ValueSourceParameters {
        val args: ListProperty<String>
    }

    override fun obtain(): String? {
        try {
            val command = listOf("git") + parameters.args.get()
            val output = ByteArrayOutputStream()
            execOperations.exec {
                commandLine = command
                standardOutput = output
                isIgnoreExitValue = true
            }

            return output.toString(Charsets.UTF_8).trim().takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            return null
        }
    }
}

fun JavaPluginExtension.javaTarget(version: Int) {
    toolchain.languageVersion.set(JavaLanguageVersion.of(version))
}
