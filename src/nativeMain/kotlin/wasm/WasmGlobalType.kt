package wasm

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class WasmGlobalType(val globalType: CPointer<wasm_globaltype_t>) : AutoCloseable {

    constructor(content: WasmValType, mutability: WasmMutability) :
            this(wasm_globaltype_new(content.valType, mutability.wasmMutability)
                ?: throw Error("failed to create global type"))

    val content: WasmValType
        get() {
            val result = wasm_globaltype_content(globalType)  ?: throw RuntimeException("Unable to get globaltype content")
            return WasmValType(result)
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
