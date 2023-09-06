plugins {
    id("net.kyori.blossom")
    id("org.jetbrains.gradle.plugin.idea-ext")
    id("via.shadow-conventions")
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", project.version.toString())
                property("impl_version", "git-ViaVersion-${project.version}:${rootProject.latestCommitHash()}")
            }
        }
    }
}

dependencies {
    api(projects.adventure) {
        targetConfiguration = "shadow"
    }
    api(libs.fastutil)
    api(libs.flare)
    api(libs.flareFastutil)
    api(libs.openNBT)
    api(libs.gson)

    compileOnlyApi(libs.snakeYaml)
    compileOnlyApi(libs.netty)
    compileOnlyApi(libs.guava)
    compileOnlyApi(libs.checkerQual)
}

java {
    withJavadocJar()
}
