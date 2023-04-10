package io.polywrap.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class GlobalType(val globalType: CPointer<wasm_globaltype_t>) : AutoCloseable {

    constructor(content: ValType.Kind, mutability: WasmMutability) :
            this(wasm_globaltype_new(ValType.allocateCValue(content), mutability.wasmMutability)
                ?: throw Error("failed to create global type"))

    val content: ValType.Kind
        get() {
            val result = wasm_globaltype_content(globalType)  ?: throw RuntimeException("Unable to get globaltype content")
            return ValType.kindFromCValue(result)
        }

    val mutability: WasmMutability
        get() {
            val result = wasm_globaltype_mutability(globalType)
            return WasmMutability.fromValue(result) ?: throw RuntimeException("Unable to get globaltype mutability")
        }

    override fun close() {
        nativeHeap.free(globalType)
    }
}
