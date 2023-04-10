package eth.krisbitney.wasmtime.wasm

import wasmtime.*

enum class WasmMutability(val wasmMutability: wasm_mutability_t) {
    CONST(wasm_mutability_enum.WASM_CONST.value.toUByte()),
    VAR(wasm_mutability_enum.WASM_VAR.value.toUByte());

    companion object {
        fun fromValue(value: wasm_mutability_t): WasmMutability? {
            return values().firstOrNull { it.wasmMutability == value }
        }
    }
}