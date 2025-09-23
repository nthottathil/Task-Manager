plugins {
    kotlin("jvm") version "1.9.21"
    application
}

group = "com.taskmanager"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // Testing dependencies
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("com.taskmanager.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.taskmanager.MainKt"
    }
}

// Create a fat JAR with all dependencies
tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    manifest {
        attributes["Main-Class"] = "com.taskmanager.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}