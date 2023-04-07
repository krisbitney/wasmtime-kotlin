plugins {
    kotlin("multiplatform") version "1.8.20"
}

group = "eth.krisbitney"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        // hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Mac OS X" -> macosArm64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        compilations.getByName("main") {
            cinterops {
                val wasmtime by creating {
                    includeDirs("wasmtime-v7.0.0-aarch64-macos-c-api/include")
                    defFile("src/nativeInterop/cinterop/wasmtime.def")
                }
            }
        }
        binaries {
            sharedLib {
                baseName = "wasmtime_kt"
                linkerOpts("-Lwasmtime-v7.0.0-aarch64-macos-c-api/lib", "-lwasmtime")
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
