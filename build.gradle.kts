plugins {
    base
    id("via.build-logic")
}

allprojects {
    group = "com.viaversion"
    version = property("projectVersion") as String // from gradle.properties
    description = "Allows the connection of newer clients to older server versions for Minecraft servers."
}

val main = setOf(
    projects.viaversion,
    projects.viaversionCommon,
    projects.viaversionApi,
    projects.viaversionBukkit,
    projects.viaversionVelocity,
    projects.viaversionFabric
).map { it.dependencyProject }

subprojects {
    when (this) {
        in main -> plugins.apply("via.shadow-conventions")
        else -> plugins.apply("via.base-conventions")
    }
}
