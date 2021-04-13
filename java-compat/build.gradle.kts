dependencies {
    api(project(":java-compat:java-compat-common"))
    api(project(":java-compat:java-compat-8"))
    api(project(":java-compat:java-compat-9"))
    api(project(":java-compat:java-compat-16"))
}

configure<JavaPluginConvention> {
    // This is necessary to allow compilation for Java 8 while still including
    // newer Java versions in the code.
    disableAutoTargetJvm()
}
