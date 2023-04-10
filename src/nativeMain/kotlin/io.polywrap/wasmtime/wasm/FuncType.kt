package io.polywrap.wasmtime.wasm

import kotlinx.cinterop.*
import toList
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class FuncType(val funcType: CPointer<wasm_functype_t>) : AutoCloseable {

    /** returned WasmValTypes are owned by WasmFuncType. Caller should not deallocate them. */
    fun params(): List<ValType<*>> {
        val params = wasm_functype_params(funcType)
            ?: throw Exception("Failed to get params from wasm_functype_t.")
        return params.toList()
    }

    /** returned WasmValTypes are owned by WasmFuncType. Caller should not deallocate them. */
    fun results(): List<ValType<*>> {
        val results = wasm_functype_results(funcType)
            ?: throw Exception("Failed to get results from wasm_functype_t.")
        return results.toList()
    }

    override fun close() {
        wasm_functype_delete(funcType)
    }
}
