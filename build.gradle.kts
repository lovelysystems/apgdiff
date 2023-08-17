plugins {
    id("com.lovelysystems.gradle") version ("1.11.5")
    application
    id("org.jetbrains.kotlinx.kover") version "0.7.3"
    kotlin("jvm") version "1.9.0"
}

repositories {
    mavenCentral()
}

group = "com.lovelysystems"

dependencies {
    implementation("io.github.java-diff-utils:java-diff-utils:4.5")
    implementation("com.github.ajalt.clikt:clikt:3.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation(kotlin("test-junit5"))
    // Note: testcontainers 1.16.0 and 1.16.2 produce flaky tests
    testImplementation("org.testcontainers:testcontainers:1.18.3")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.2.0")

    implementation("org.slf4j:slf4j-api:2.0.7")
    testImplementation("ch.qos.logback:logback-classic:1.4.11")
}

application {
    mainClass.set("cz.startnet.utils.pgdiff.CLIKt")
    applicationName = "apgdiff"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

val fatJar by tasks.creating(Jar::class) {
    archiveClassifier.set("fat")
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map {
        {
            if (it.isDirectory) {
                it
            } else {
                zipTree(it)
            }
        }
    })
    with(tasks["jar"] as CopySpec)
}

lovely {
    gitProject()
    dockerProject("lovelysystems/apgdiff") {
        from(tasks["fatJar"].outputs)
    }
}

