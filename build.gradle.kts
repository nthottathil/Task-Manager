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
    // Kotlin standard library
    implementation(kotlin("stdlib"))
}

kotlin {
    // Use Java 11
    jvmToolchain(11)
}

application {
    // Main class for the TaskApp.kt file
    mainClass.set("TaskAppKt")
}

// Task to create executable JAR
tasks.jar {
    manifest {
        attributes["Main-Class"] = "TaskAppKt"
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
        attributes["Main-Class"] = "TaskAppKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Custom run task with better error handling
tasks.run.configure {
    standardInput = System.`in`
}

// Clean task enhancement
tasks.clean {
    doLast {
        println("Clean completed")
    }
}