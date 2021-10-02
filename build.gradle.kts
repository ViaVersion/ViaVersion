import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME

plugins {
    base
    id("via.build-logic")
}

allprojects {
    group = "com.viaversion"
    version = property("projectVersion") as String // from gradle.properties
    description = "Allow newer clients to join older server versions."
}

val platforms = setOf(
    projects.viaversionBukkit,
    projects.viaversionBungee,
    projects.viaversionFabric,
    projects.viaversionSponge,
    projects.viaversionVelocity
).map { it.dependencyProject }

val special = setOf(
    projects.viaversion,
    projects.viaversionApi,
    projects.adventure
).map { it.dependencyProject }

subprojects {
    when (this) {
        in platforms -> plugins.apply("via.platform-conventions")
        in special -> plugins.apply("via.base-conventions")
        else -> plugins.apply("via.standard-conventions")
    }

    // Note: If manually starting tests doesn't work for you in IJ, change 'Gradle -> Run Tests Using' to 'IntelliJ IDEA'
    dependencies {
        // The alternative to this long boi is writing "testImplementation", including the quotes
        TEST_IMPLEMENTATION_CONFIGURATION_NAME(rootProject.libs.netty)
        TEST_IMPLEMENTATION_CONFIGURATION_NAME(rootProject.libs.guava)
        TEST_IMPLEMENTATION_CONFIGURATION_NAME(rootProject.libs.bundles.junit)
    }
}
