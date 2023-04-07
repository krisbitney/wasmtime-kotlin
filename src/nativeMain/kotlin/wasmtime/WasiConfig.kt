package wasmtime

import kotlinx.cinterop.*
import platform.posix.memcpy

@OptIn(ExperimentalStdlibApi::class)
class WasiConfig(val wasiConfig: CPointer<wasi_config_t>) : AutoCloseable {

    constructor() : this(wasi_config_new() ?: throw Exception("failed to create WASI config"))

    fun setArgv(argv: List<String>) = memScoped {
        val size = argv.size
        val cArgv = argv.map { it.cstr.ptr }.toCValues()
        wasi_config_set_argv(wasiConfig, size, cArgv)
    }

    fun inheritArgv() {
        wasi_config_inherit_argv(wasiConfig)
    }

    fun setEnv(env: Map<String, String>) = memScoped {
        val size = env.size
        val names = env.keys.map { it.cstr.ptr }.toCValues()
        val values = env.values.map { it.cstr.ptr }.toCValues()
        wasi_config_set_env(wasiConfig, size, names, values)
    }

    fun inheritEnv() {
        wasi_config_inherit_env(wasiConfig)
    }

    fun setStdinFile(path: String): Boolean {
        return wasi_config_set_stdin_file(wasiConfig, path)
    }

    fun setStdinBytes(bytes: ByteArray) {
        val wasmBytes = nativeHeap.alloc<wasm_byte_vec_t>()
        wasmBytes.size = bytes.size.convert()
        wasmBytes.data = bytes.usePinned { pinned ->
            val ptr = nativeHeap.allocArray<ByteVar>(bytes.size)
            memcpy(ptr, pinned.addressOf(0), bytes.size.convert())
            ptr
        }
        wasi_config_set_stdin_bytes(wasiConfig, wasmBytes.ptr)
    }

    fun inheritStdin() {
        wasi_config_inherit_stdin(wasiConfig)
    }

    fun setStdoutFile(path: String): Boolean {
        return wasi_config_set_stdout_file(wasiConfig, path)
    }

    fun inheritStdout() {
        wasi_config_inherit_stdout(wasiConfig)
    }

    fun setStderrFile(path: String): Boolean {
        return wasi_config_set_stderr_file(wasiConfig, path)
    }

    fun inheritStderr() {
        wasi_config_inherit_stderr(wasiConfig)
    }

    fun preopenDir(path: String, guestPath: String): Boolean {
        return wasi_config_preopen_dir(wasiConfig, path, guestPath)
    }

    fun preopenSocket(fdNum: UInt, hostPort: String): Boolean {
        return wasi_config_preopen_socket(wasiConfig, fdNum, hostPort)
    }

    override fun close() {
        wasi_config_delete(wasiConfig)
    }
}
