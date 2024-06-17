plugins {
    kotlin("multiplatform") version "2.0.0"
    id("com.lovelysystems.gradle") version ("1.12.0")
    application
    id("org.jetbrains.kotlinx.kover") version "0.7.3"
    //kotlin("jvm") version "2.0.0"
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
//    macosX64{
//        binaries.executable()
//    }

    sourceSets {
        commonMain {
            dependencies {
                //implementation("io.github.java-diff-utils:java-diff-utils:4.5")
                implementation("io.github.petertrr:kotlin-multiplatform-diff:0.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.4.0")
                implementation("com.github.ajalt.clikt:clikt:3.3.0")

                implementation("org.slf4j:slf4j-api:2.0.7")
            }

        }
        jvmTest {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:5.8.1")
                implementation(kotlin("test-junit5"))
                implementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

                // Note: testcontainers 1.16.0 and 1.16.2 produce flaky tests
                implementation("org.testcontainers:testcontainers:1.18.3")
                implementation("io.kotest:kotest-assertions-core-jvm:4.2.0")

                implementation("org.slf4j:slf4j-api:2.0.7")
                implementation("ch.qos.logback:logback-classic:1.4.11")
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
        platforms = listOf("linux/amd64"),
        buildPlatforms = listOf("linux/amd64")
    ) {
        from(tasks["linkReleaseExecutableLinuxX64"].outputs)
    }
}

