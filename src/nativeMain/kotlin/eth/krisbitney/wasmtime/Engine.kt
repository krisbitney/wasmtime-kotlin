package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import wasmtime.*

/**
 * An [Engine] is a compilation environment and configuration
 * used for compiling WebAssembly code. Engines are typically global in a program and contain all the
 * necessary configuration for compiling WebAssembly code.
 *
 * An [Engine] is safe to share between threads. Multiple [Store]s can be created within the same
 * engine with each store living on a separate thread. It is recommended to create one [Engine]
 * for the lifetime of your program.
 *
 * @property engine A pointer to the underlying Wasmtime engine.
 */
@OptIn(ExperimentalStdlibApi::class)
class Engine(val engine: CPointer<wasm_engine_t>) : AutoCloseable {

    /**
     * Creates a new [Engine] with the default configuration.
     *
     * @throws RuntimeException if the engine could not be created.
     */
    constructor() :
            this(wasm_engine_new() ?: throw RuntimeException("Failed to create Wasmtime engine"))

    /**
     * Creates a new [Engine] with the specified [configure] function applied to the engine's [Config].
     *
     * @param configure A lambda that configures the engine's [Config].
     * @throws RuntimeException if the engine could not be created with the provided config.
     */
    constructor (configure: Config.() -> Unit) : this(Config().run {
        this.apply(configure)
        wasm_engine_new_with_config(config)
            ?: throw RuntimeException("Failed to create Wasmtime engine with provided config")
    })

    /**
     * Increments the engine-local epoch variable.
     *
     * This function increments the engine's current epoch which can be used to
     * force WebAssembly code to trap if the current epoch goes beyond the
     * [Store] configured epoch deadline.
     *
     * This function is safe to call from any thread and is also async-signal-safe.
     */
    fun incrementEpoch() {
        wasmtime_engine_increment_epoch(engine)
    }

    override fun close() {
        wasm_engine_delete(engine)
    }
}
