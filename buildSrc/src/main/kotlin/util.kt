
fun getWasmtimeTarget(kotlinTarget: String): String {
    return when (kotlinTarget) {
        "macosArm64", "native" -> "aarch64-macos"
        "macosX64" -> "x86_64-macos"
        "linuxArm64" -> "aarch64-linux"
        "linuxX64" -> "x86_64-linux"
        "mingwX64" -> "x86_64-mingw"
        else -> throw Exception("Unknown target: $kotlinTarget")
    }
}

// get the correct compressed file extension for the target
fun compressionExtension(cApiTarget: String): String {
    return if (cApiTarget.contains("mingw")) "zip" else "tar.xz"
}
