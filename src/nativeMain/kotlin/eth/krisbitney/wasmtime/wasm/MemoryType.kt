package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

/**
 * A Kotlin/Native wrapper for the `wasm_memorytype_t` struct, representing the type of a WebAssembly memory.
 *
 * @property memoryType The C pointer to the `wasm_memorytype_t` struct.
 * @constructor Creates a new [MemoryType] instance from the given C pointer.
 * @constructor Creates a new [MemoryType] instance with the specified minimum and maximum memory limits.
 */
@OptIn(ExperimentalStdlibApi::class)
class MemoryType(val memoryType: CPointer<wasm_memorytype_t>) : AutoCloseable {

    /**
     * Creates a new [MemoryType] instance with the specified minimum and maximum memory limits.
     *
     * @param min The minimum size of the memory in WebAssembly pages.
     * @param max The maximum size of the memory in WebAssembly pages.
     */
    constructor(min: UInt = 0u, max: UInt = WasmLimits.LIMITS_MAX_DEFAULT) :
            this(
                min.run {
                    val limits = WasmLimits.cLimits(min, max).ptr
                    val memoryType = wasm_memorytype_new(WasmLimits.cLimits(min, max).ptr)
                        ?: throw Error("failed to create memory type")
                    nativeHeap.free(limits)
                    memoryType
                }
            )

    /**
     * Retrieves the limits of the WebAssembly memory as a [WasmLimits] instance.
     *
     * @return The limits of the memory.
     */
    val limits: WasmLimits
        get() {
        val ptr = wasm_memorytype_limits(memoryType) ?: throw Error("failed to get memory limits")
        return WasmLimits(ptr.pointed.min, ptr.pointed.max)
    }
    override fun close() {
        wasm_memorytype_delete(memoryType)
    }
}
