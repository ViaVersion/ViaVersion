dependencies {
    api(project(":java-compat:java-compat-common"))
}

configure<JavaPluginConvention> {
    // This is for Java 16, but the minimum required for this
    // is actually just Java 9!
    sourceCompatibility = JavaVersion.VERSION_1_9
    targetCompatibility = JavaVersion.VERSION_1_9
}
