plugins {
    id("com.lovelysystems.gradle") version ("1.3.2")
    application
    kotlin("jvm") version "1.4.32"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.java-diff-utils:java-diff-utils:4.5")
    implementation("com.github.ajalt.clikt:clikt:3.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.testcontainers:testcontainers:1.15.2")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.2.0")
}

application {
    mainClass.set("cz.startnet.utils.pgdiff.CLIKt")
    applicationName = "apgdiff"
}

lovely {
    gitProject()
    dockerProject("lovelysystems/apgdiff")
    dockerFiles.from(tasks["distTar"].outputs)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
