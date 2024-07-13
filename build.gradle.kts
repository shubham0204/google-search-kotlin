plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.dokka") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}

group = "com.github.shubham0204"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create( "release" , MavenPublication::class.java ) {
            from(components["java"])
            groupId = "com.github.shubham0204"
            artifactId = "google_search_kotlin"
            version = "0.0.1"
        }
    }
}