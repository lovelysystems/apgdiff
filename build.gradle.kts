plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.lovelysystemsGradle)
    application
    alias(libs.plugins.kotlinxKover)
}

repositories {
    mavenCentral()
}
group = "com.lovelysystems"

kotlin {

    jvm {
        withJava()
    }
    linuxX64 {
        binaries.executable()
    }

    linuxArm64 {
        binaries.executable()
    }

    macosArm64 {
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.multiplatform.diff)
                implementation(libs.kotlinx.io.core)
                implementation(libs.clikt)
            }

        }
        commonTest {
            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(kotlin("test"))
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.params)
                implementation(kotlin("test-junit5"))
                implementation(libs.testcontainers)
                implementation(libs.logback.classic)
            }

        }

    }

}

application {
    mainClass.set("cz.startnet.utils.pgdiff.CLIKt")
    applicationName = "apgdiff"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

lovely {
    gitProject()
    dockerProject(
        "lovelysystems/apgdiff",
        platforms = listOf("linux/amd64", "linux/arm64"),
    ) {
        into("amd64") {
            from(tasks["linkReleaseExecutableLinuxX64"].outputs)
        }
        into("arm64") {
            from(tasks["linkReleaseExecutableLinuxArm64"].outputs)
        }
    }
}
