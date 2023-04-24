plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.tukaani:xz:1.9")
}

tasks.named("jar").configure {
    enabled = false
}