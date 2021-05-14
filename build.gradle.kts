plugins {
    id("com.lovelysystems.gradle") version ("1.3.2")
    application
    jacoco
    kotlin("jvm") version "1.5.0"
}

repositories {
    mavenCentral()
}

group = "com.lovelysystems"

jacoco {
    toolVersion = "0.8.6"
    reportsDirectory.set(buildDir.resolve("coverage"))
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

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.isEnabled = false
        csv.isEnabled = false
        html.destination = file(buildDir.resolve("coverage/html"))
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestReport)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val fatJar by tasks.creating(Jar::class) {
    archiveClassifier.set("fat")
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
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
    dockerProject("lovelysystems/apgdiff")
    dockerFiles.from(tasks["fatJar"].outputs)
}

