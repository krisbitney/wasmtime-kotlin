package wasm

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class WasmImportType(val importType: CPointer<wasm_importtype_t>) : AutoCloseable {

    constructor(module: String, name: String, externType: WasmExternType) : this(
        externType.let {
            val modulePtr = cValue<wasm_name_t>()
            wasm_name_new_from_string(modulePtr, module)
            val namePtr = cValue<wasm_name_t>()
            wasm_name_new_from_string(namePtr, name)

            wasm_importtype_new(modulePtr, namePtr, it.externType)
                ?: throw Error("Failed to create WasmImportType")
        }
    )

    val module: String
        get() {
            val namePtr = wasm_importtype_module(importType) ?: throw Error("Failed to get module name")
            return namePtr.pointed.data!!.toKString()
        }

    val name: String
        get() {
            val namePtr = wasm_importtype_name(importType) ?: throw Error("Failed to get import name")
            return namePtr.pointed.data!!.toKString()
        }

    val type: WasmExternType
        get() {
            val ptr = wasm_importtype_type(importType) ?: throw Error("Failed to get import type")
            return WasmExternType(ptr)
        }

    override fun close() {
        wasm_importtype_delete(importType)
    }
}
