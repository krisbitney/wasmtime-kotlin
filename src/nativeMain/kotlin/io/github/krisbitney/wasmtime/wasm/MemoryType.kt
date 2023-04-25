package io.github.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

/**
 * Represents a WebAssembly memory type.
 *
 * @property limits The memory limits associated with this memory type, in units of WebAssembly pages (64 KiB).
 */
data class MemoryType(val limits: Limits) : ExternType(ExternType.Kind.MEMORY) {

    /**
     * Creates a new [MemoryType] instance with the given minimum and maximum pages.
     *
     * @param min The minimum value required.
     * @param max The maximum value required, or [Limits.LIMITS_MAX_DEFAULT] if no maximum is specified.
     */
    constructor(min: UInt = 0u, max: UInt = Limits.LIMITS_MAX_DEFAULT) : this(Limits(min, max))

    /**
     * Creates a new [MemoryType] instance from the given C pointer.
     *
     * @param memoryType The C pointer to the `wasm_memorytype_t` struct.
     * @param ownedByCaller Whether the caller owns the memory type and is responsible for freeing it.
     * @throws Error If there is a failure to get memory limits from the underlying `wasm_memorytype_t`.
     */
    constructor(memoryType: CPointer<wasm_memorytype_t>, ownedByCaller: Boolean = false) : this(
        memoryType.run {
            val ptr = wasm_memorytype_limits(memoryType) ?: throw Exception("failed to get memory limits")
            Limits(ptr.pointed.min, ptr.pointed.max)
        }
    ) {
        if (!ownedByCaller) wasm_memorytype_delete(memoryType)
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
            val cLimits = Limits.allocateCValue(limits.min, limits.max)
            val cMemoryType = wasm_memorytype_new(cLimits)
            nativeHeap.free(cLimits)
            if (cMemoryType == null) {
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
