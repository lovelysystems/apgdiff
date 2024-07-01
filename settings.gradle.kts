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

    // Catalogs
    versionCatalogs {

        create("libs") {
            library("kotlin-multiplatform-diff", "io.github.petertrr", "kotlin-multiplatform-diff")
                .version("0.7.0")
            library("kotlinx-io-core", "org.jetbrains.kotlinx", "kotlinx-io-core")
                .version("0.4.0")
            library("clikt", "com.github.ajalt.clikt", "clikt").version("4.4.0")
            library("logback-classic", "ch.qos.logback", "logback-classic").version("1.4.11")
        }

        // Testing
        create("testLibs") {
            library("kotest-assertions-core", "io.kotest", "kotest-assertions-core").version("5.9.1")
            library("testcontainers", "org.testcontainers", "testcontainers").version("1.18.3")
            library("junit-jupiter-api", "org.junit.jupiter", "junit-jupiter-api").withoutVersion()
            library("junit-jupiter-params", "org.junit.jupiter", "junit-jupiter-params").withoutVersion()
        }

    }
}
