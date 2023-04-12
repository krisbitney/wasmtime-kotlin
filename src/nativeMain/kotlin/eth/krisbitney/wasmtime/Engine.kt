package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class Engine(val engine: CPointer<wasm_engine_t>) : AutoCloseable {

    constructor() :
            this(wasm_engine_new() ?: throw RuntimeException("Failed to create Wasmtime engine"))

    constructor(config: Config) :
            this(wasm_engine_new_with_config(config.config)
                ?: throw RuntimeException("Failed to create Wasmtime engine with provided config"))

    fun incrementEpoch() {
        wasmtime_engine_increment_epoch(engine)
    }

    override fun close() {
        wasm_engine_delete(engine)
    }
}
