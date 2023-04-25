package io.github.krisbitney.wasmtime

import kotlinx.cinterop.*
import platform.posix.memcpy
import wasmtime.*

/**
 * The [WasiConfig] class is used for configuring a WASI instance.
 * It manages WASI command-line arguments, environment variables, standard input/output/error,
 * and preopened directories and sockets.
 *
 * This class should not be instantiated directly. Instead, use WasiConfig builder method in the [Store]'s [Context].
 *
 * @property wasiConfig The pointer to the native wasi_config_t object.
 */
@OptIn(ExperimentalStdlibApi::class)
class WasiConfig(val wasiConfig: CPointer<wasi_config_t>) : AutoCloseable {

    /**
     * Constructs a new [WasiConfig] instance by creating a new native WASI configuration object.
     *
     * @throws Exception if the native WASI configuration object could not be created.
     */
    constructor() : this(wasi_config_new() ?: throw Exception("failed to create WASI config"))

    /**
     * Sets the command-line arguments for the WASI instance.
     *
     * @param argv A list of command-line arguments to pass to the WASI instance.
     * @return This [WasiConfig] instance.
     */
    fun setArgv(argv: List<String>): WasiConfig = this.apply {
        memScoped {
            val size = argv.size
            val cArgv = argv.map { it.cstr.ptr }.toCValues()
            wasi_config_set_argv(wasiConfig, size, cArgv)
        }
    }

    /**
     * Inherit command-line arguments from the current process for the WASI instance.
     *
     * @return This [WasiConfig] instance.
     */
    fun inheritArgv(): WasiConfig = this.apply {
        wasi_config_inherit_argv(wasiConfig)
    }

    /**
     * Sets the environment variables for the WASI instance.
     *
     * @param env A map of environment variable names and their corresponding values.
     * @return This [WasiConfig] instance.
     */
    fun setEnv(env: Map<String, String>): WasiConfig = this.apply {
        memScoped {
            val size = env.size
            val names = env.keys.map { it.cstr.ptr }.toCValues()
            val values = env.values.map { it.cstr.ptr }.toCValues()
            wasi_config_set_env(wasiConfig, size, names, values)
        }
    }

    /**
     * Inherit the environment variables from the current process for the WASI instance.
     *
     * @return This [WasiConfig] instance.
     */
    fun inheritEnv(): WasiConfig = this.apply {
        wasi_config_inherit_env(wasiConfig)
    }

    /**
     * Sets the standard input to be read from the specified file for the WASI instance.
     *
     * @param path The path to the file to be used as standard input.
     * @return `true` if the standard input was successfully set, `false` otherwise.
     */
    fun setStdinFile(path: String): Boolean {
        return wasi_config_set_stdin_file(wasiConfig, path)
    }

    /**
     * Sets the standard input to be read from the specified byte array for the WASI instance.
     *
     * @param bytes The byte array to be used as standard input.
     * @return This [WasiConfig] instance.
     */
    fun setStdinBytes(bytes: ByteArray): WasiConfig = this.apply {
        val wasmBytes = nativeHeap.alloc<wasm_byte_vec_t> {
            size = bytes.size.convert()
            data = bytes.usePinned { pinned ->
                val ptr = nativeHeap.allocArray<ByteVar>(bytes.size)
                memcpy(ptr, pinned.addressOf(0), bytes.size.convert())
                ptr
            }
        }
        wasi_config_set_stdin_bytes(wasiConfig, wasmBytes.ptr)
    }

    /**
     * Inherit the standard input from the current process for the WASI instance.
     *
     * @return This [WasiConfig] instance.
     */
    fun inheritStdin(): WasiConfig = this.apply {
        wasi_config_inherit_stdin(wasiConfig)
    }

    /**
     * Sets the standard output to be written to the specified file for the WASI instance.
     *
     * @param path The path to the file to be used as standard output.
     * @return `true` if the standard output was successfully set, `false` otherwise.
     */
    fun setStdoutFile(path: String): Boolean {
        return wasi_config_set_stdout_file(wasiConfig, path)
    }

    /**
     * Inherit the standard output from the current process for the WASI instance.
     *
     * @return This [WasiConfig] instance.
     */
    fun inheritStdout(): WasiConfig = this.apply {
        wasi_config_inherit_stdout(wasiConfig)
    }

    /**
     * Sets the standard error output to be written to the specified file for the WASI instance.
     *
     * @param path The path to the file to be used as standard error output.
     * @return `true` if the standard error output was successfully set, `false` otherwise.
     */
    fun setStderrFile(path: String): Boolean {
        return wasi_config_set_stderr_file(wasiConfig, path)
    }

    /**
     * Inherit the standard error output from the current process for the WASI instance.
     *
     * @return This [WasiConfig] instance.
     */
    fun inheritStderr(): WasiConfig = this.apply {
        wasi_config_inherit_stderr(wasiConfig)
    }

    /**
     * Preopens a directory on the host file system for the WASI instance.
     *
     * This function grants the WASI instance access to a specific directory and its contents.
     * The WASI instance will have access to the specified directory but nothing above it.
     *
     * @param path The path to the directory on the host file system.
     * @param guestPath The name by which the directory will be known in the WASI instance.
     * @return `true` if the directory was successfully preopened, `false` otherwise.
     */
    fun preopenDir(path: String, guestPath: String): Boolean {
        return wasi_config_preopen_dir(wasiConfig, path, guestPath)
    }

    /**
     * Preopens a socket for the WASI instance.
     *
     * By default, WASI programs do not have access to open network sockets on the host.
     * This function grants the WASI instance access to a network socket file descriptor on the host.
     * The WASI instance will be able to use the preopened socket for network communication.
     *
     * @param fdNum The number of the file descriptor by which the socket will be known in the WASI instance.
     * @param hostPort The IP address and port (e.g. "127.0.0.1:8080") requested to listen on.
     * @return `true` if the socket was successfully preopened, `false` otherwise.
     */
    fun preopenSocket(fdNum: UInt, hostPort: String): Boolean {
        return wasi_config_preopen_socket(wasiConfig, fdNum, hostPort)
    }

    override fun close() {
        wasi_config_delete(wasiConfig)
    }
}
