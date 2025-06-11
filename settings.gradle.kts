pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "apgdiff"

// load the local properties file if it exists
file("local.properties").takeIf { it.exists() }?.let {
    val p = java.util.Properties()
    p.load(it.reader())
    for (entry in p) {
        settings.extra.set(entry.key as String, entry.value)
    }
}

val kotlinMultiplatformDiff: String? by settings.extra

// if kotlinMultiplatformDiff is defined, we use it as path to the
// https://github.com/petertrr/kotlin-multiplatform-diff checkout and include it as a build
kotlinMultiplatformDiff?.let {

    includeBuild(it) {
        dependencySubstitution {
            substitute(module("io.github.petertrr:kotlin-multiplatform-diff"))
                .using(project(":"))
        }
    }
}


dependencyResolutionManagement {

    repositories {
        mavenCentral()
    }

    // Version catalog is defined in gradle/libs.versions.toml
}
