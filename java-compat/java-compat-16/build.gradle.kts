dependencies {
    api(projects.javaCompat.javaCompatCommon)
}

// This is for Java 16, but the minimum required for this
// is actually just Java 9!
configureJavaTarget(9)
