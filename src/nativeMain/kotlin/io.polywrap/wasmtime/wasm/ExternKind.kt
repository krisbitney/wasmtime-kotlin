package io.polywrap.wasmtime.wasm

import wasmtime.*

enum class ExternKind(val value: wasm_externkind_t) {
    FUNC(wasm_externkind_enum.WASM_EXTERN_FUNC.value.toUByte()),
    GLOBAL(wasm_externkind_enum.WASM_EXTERN_GLOBAL.value.toUByte()),
    TABLE(wasm_externkind_enum.WASM_EXTERN_TABLE.value.toUByte()),
    MEMORY(wasm_externkind_enum.WASM_EXTERN_MEMORY.value.toUByte());

    companion object {
        fun fromValue(value: wasm_externkind_t): ExternKind {
            return values().find { it.value == value }  ?: throw IllegalArgumentException("Invalid ExternKind value: $value")
        }
    }
}