package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

/**
 * Represents a WebAssembly memory type.
 *
 * @property limits The memory limits associated with this memory type.
 *
 * @constructor Creates a new [MemoryType] instance with the given memory limits.
 * @param limits The [WasmLimits] defining the memory limits.
 *
 * @constructor Creates a new [MemoryType] instance from the given C pointer.
 * @param memoryType The C pointer to the `wasm_memorytype_t` struct.
 * @throws Error If there is a failure to get memory limits from the underlying `wasm_memorytype_t`.
 */
class MemoryType(val limits: WasmLimits) : ExternType(ExternType.Kind.MEMORY) {

    constructor(memoryType: CPointer<wasm_memorytype_t>) : this(
        memoryType.run {
            val ptr = wasm_memorytype_limits(memoryType) ?: throw Exception("failed to get memory limits")
            WasmLimits(ptr.pointed.min, ptr.pointed.max)
        }
    ) {
        wasm_memorytype_delete(memoryType)
    }

    /**
     * Companion object providing utility methods for working with C values and pointers
     * related to the [MemoryType] class.
     */
    companion object {
        /**
         * Allocates a new C pointer for the given [MemoryType] and creates a `wasm_memorytype_t` instance.
         *
         * @param memoryType The [MemoryType] to be allocated.
         * @return The newly created C pointer to a `wasm_memorytype_t`.
         * @throws Error If there is a failure to create the memory type.
         */
        fun allocateCValue(memoryType: MemoryType): CPointer<wasm_memorytype_t> {
            val limits = memoryType.limits
            val cLimits = WasmLimits.allocateCValue(limits.min, limits.max)
            val cMemoryType = wasm_memorytype_new(cLimits.ptr)
            if (cMemoryType == null) {
                nativeHeap.free(cLimits.ptr)
                throw Exception("failed to create memory type")
            }
            return cMemoryType
        }

        /**
         * Deletes the C value for the given `wasm_memorytype_t` pointer.
         *
         * @param memoryType The C pointer to the `wasm_memorytype_t` to be deleted.
         */
        fun deleteCValue(memoryType: CPointer<wasm_memorytype_t>) {
            wasm_memorytype_delete(memoryType)
        }
    }
}
