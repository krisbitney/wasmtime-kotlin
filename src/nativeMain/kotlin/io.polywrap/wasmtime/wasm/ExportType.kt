package io.polywrap.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class ExportType(val exportType: CPointer<wasm_exporttype_t>) : AutoCloseable {

    constructor(name: String, externType: ExternType) : this(
        externType.let {
            val namePtr = cValue<wasm_name_t> { }
            wasm_name_new_from_string(namePtr, name)

            wasm_exporttype_new(namePtr, it.externType)
                ?: throw Error("Failed to create WasmExportType")
        }
    )

    val name: String
        get() {
            val namePtr = wasm_exporttype_name(exportType) ?: throw Error("Failed to get export name")
            return namePtr.pointed.data!!.toKString()
        }

    val type: ExternType
        get() {
            val ptr = wasm_exporttype_type(exportType) ?: throw Error("Failed to get export type")
            return ExternType(ptr)
        }

    override fun close() {
        wasm_exporttype_delete(exportType)
    }
}
