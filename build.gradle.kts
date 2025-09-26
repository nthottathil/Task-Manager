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
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("SimpleTaskAppKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "SimpleTaskAppKt"
    }
}