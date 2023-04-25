package io.github.krisbitney.wasmtime.wasm

import wasmtime.*

/**
 * Enum class representing the mutability of WebAssembly globals.
 *
 * @property wasmMutability The mutability value as [wasm_mutability_t] from the WebAssembly C API.
 * @constructor Constructs a [Mutability] instance based on the provided wasmMutability value.
 */
enum class Mutability(val wasmMutability: wasm_mutability_t) {
    /**
     * Represents a constant (immutable) global.
     */
    CONST(wasm_mutability_enum.WASM_CONST.value.toUByte()),
    /**
     * Represents a variable (mutable) global.
     */
    VAR(wasm_mutability_enum.WASM_VAR.value.toUByte());

    companion object {
        /**
         * Retrieves the [Mutability] instance corresponding to the provided [value].
         *
         * @param value The [wasm_mutability_t] value for which to retrieve the [Mutability] instance.
         * @return The [Mutability] instance corresponding to the provided value, or `null` if no matching instance is found.
         */
        fun fromValue(value: wasm_mutability_t): Mutability? {
            return values().firstOrNull { it.wasmMutability == value }
        }
    }
}