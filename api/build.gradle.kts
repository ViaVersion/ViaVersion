plugins {
    id("net.kyori.blossom")
    id("org.jetbrains.gradle.plugin.idea-ext")
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
    api(libs.fastutil)
    api(libs.vianbt)
    api(libs.gson)
    implementation(rootProject.libs.text) {
        exclude("com.google.code.gson", "gson")
        exclude("com.viaversion", "nbt")
    }

    compileOnlyApi(libs.snakeYaml)
    compileOnlyApi(libs.netty)
    compileOnlyApi(libs.guava)
    compileOnlyApi(libs.checkerQual)
}

java {
    withJavadocJar()
}

publishShadowJar()
