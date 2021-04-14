dependencies {
    api(projects.javaCompat.javaCompatCommon)
    api(projects.javaCompat.javaCompat8)
    api(projects.javaCompat.javaCompat9)
    api(projects.javaCompat.javaCompat16)
}

configure<JavaPluginConvention> {
    // This is necessary to allow compilation for Java 8 while still including
    // newer Java versions in the code.
    disableAutoTargetJvm()
}
