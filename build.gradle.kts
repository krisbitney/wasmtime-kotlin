import org.jetbrains.kotlin.de.undercouch.gradle.tasks.download.Download

plugins {
    kotlin("multiplatform") version "1.8.20"
    id("de.undercouch.download") version "5.4.0"
}

group = "eth.krisbitney"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val wasmtimeDownloadDir = "wasmtime-c-api"
val wasmtimeCApiVersion = "8.0.0"
val wasmtimeCApiTargets = listOf(
    "aarch64-macos",
    "x86_64-macos",
    "aarch64-linux",
    "x86_64-linux",
    // TODO: can only link needed mingw libs on windows machine
//    "x86_64-mingw"
)

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    val nativeTargets = listOf(
        macosArm64("native"),
        macosX64("macosX64"),
        linuxX64("linuxX64"),
        linuxArm64("linuxArm64"),
//        mingwX64("mingwX64"),
    )

    nativeTargets.forEach {
        it.apply {
            compilations.getByName("main") {
                cinterops {
                    val wasmtime by creating {
                        val cApiTarget = getWasmtimeTarget(it.name)
                        val wasmtimeTargetDir = "$wasmtimeDownloadDir/wasmtime-v$wasmtimeCApiVersion-$cApiTarget-c-api"
                        extraOpts("-libraryPath", "$projectDir/$wasmtimeTargetDir/lib")
                        includeDirs("$wasmtimeTargetDir/include")
                        defFile("src/nativeInterop/cinterop/wasmtime.def")
                    }
                }
                defaultSourceSet.dependsOn(sourceSets["nativeMain"])
            }
            compilations.getByName("test") {
                defaultSourceSet.dependsOn(sourceSets["nativeTest"])
            }
        }
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}

// Download the wasmtime c api (if not already exists)
tasks.register<Download>("downloadWasmtimeCApi") {
    val baseUrl = "https://github.com/bytecodealliance/wasmtime/releases/download"
    val urls = wasmtimeCApiTargets.map {
        val extension = compressionExtension(it)
        "$baseUrl/v$wasmtimeCApiVersion/wasmtime-v$wasmtimeCApiVersion-$it-c-api.$extension"
    }
    src(urls)
    dest("$projectDir/$wasmtimeDownloadDir")
    overwrite(false)
}

// Unzip the wasmtime c api (if not already unpacked)
tasks.register<Copy>("unpackWasmtimeCApi") {
    dependsOn("downloadWasmtimeCApi")
    val downloadTask = tasks.getByName("downloadWasmtimeCApi") as Download
    val basePath = downloadTask.dest.path
    val unpacked = wasmtimeCApiTargets.map {
        val extension = compressionExtension(it)
        val compressedFilePath = "$basePath/wasmtime-v$wasmtimeCApiVersion-$it-c-api.$extension"
        when (extension) {
            "zip" -> zipTree(compressedFilePath)
            "tar.xz" -> tarTree(resources.xz(compressedFilePath))
            else -> throw Exception("Unknown file extension: $extension")
        }
    }
    from(unpacked)
    into(downloadTask.dest)
    onlyIf {
        wasmtimeCApiTargets.firstOrNull()?.let {
            val unpackedFilePath = "$basePath/wasmtime-v$wasmtimeCApiVersion-$it-c-api"
            !File(unpackedFilePath).exists()
        } ?: false
    }
}

// set c interops dependency to make sure the wasmtime c api is available for each target
tasks.withType<org.jetbrains.kotlin.gradle.tasks.CInteropProcess>().configureEach {
    dependsOn(tasks.getByName("unpackWasmtimeCApi"))
    mustRunAfter(tasks.getByName("unpackWasmtimeCApi"))
}

// set build dependency to make sure the wasmtime c api is available for each target
tasks.build {
    dependsOn(tasks.getByName("unpackWasmtimeCApi"))
    mustRunAfter(tasks.getByName("unpackWasmtimeCApi"))
}

// print stdout during tests
tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}
