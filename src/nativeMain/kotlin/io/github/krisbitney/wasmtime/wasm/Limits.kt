package io.github.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.wasm_limits_t

/**
 * Represents the limits for tables and memories in a WebAssembly module.
 *
 * @property min The minimum value required.
 * @property max The maximum value required, or [LIMITS_MAX_DEFAULT] if no maximum is specified.
 *
 * @constructor Constructs a new [Limits] instance with the given minimum and maximum values.
 * @param min The minimum value required.
 * @param max The maximum value required, or [LIMITS_MAX_DEFAULT] if no maximum is specified.
 *
 * @companion object Contains utility methods and constants related to [Limits].
 */
data class Limits(val min: UInt = 0u, val max: UInt = LIMITS_MAX_DEFAULT) {
    companion object {
        /**
         * The default maximum value for limits in WebAssembly modules.
         */
        const val LIMITS_MAX_DEFAULT: UInt = 0xffffffffu

        /**
         * Creates a [wasm_limits_t] instance from the given minimum and maximum values.
         *
         * @param min The minimum value required.
         * @param max The maximum value required, or [LIMITS_MAX_DEFAULT] if no maximum is specified.
         * @return A [wasm_limits_t] instance with the specified minimum and maximum values.
         */
        fun allocateCValue(min: UInt = 0u, max: UInt = LIMITS_MAX_DEFAULT): CPointer<wasm_limits_t> {
            val cLimits = nativeHeap.alloc<wasm_limits_t>()
            cLimits.min = min
            cLimits.max = max
            return cLimits.ptr
        }
    }
}
