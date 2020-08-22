plugins {
    id("com.lovelysystems.gradle") version ("1.3.2")
    application
    kotlin("jvm") version "1.4.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation("org.hamcrest:hamcrest-all:1.3")
}

application {
    mainClassName = "cz.startnet.utils.pgdiff.Main"
    applicationName = "apgdiff"
}

lovely {
    gitProject()
    dockerProject("lovelysystems/apgdiff")
    dockerFiles.from(tasks["distTar"].outputs)
}

tasks.withType<Test> {
    useJUnit()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
