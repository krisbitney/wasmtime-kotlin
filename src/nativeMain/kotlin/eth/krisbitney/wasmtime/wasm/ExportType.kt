package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

/**
 * Represents an export type in WebAssembly.
 *
 * @property name The name of the exported item.
 * @property type The [ExternType] of the exported item.
 *
 * @constructor Creates a new [ExportType] instance with the given name and [ExternType].
 * @param name The name of the exported item.
 * @param type The [ExternType] of the exported item.
 */
class ExportType(val name: String, val type: ExternType) {

    /**
     * Creates a new [ExportType] instance from the given C pointer to a `wasm_exporttype_t` struct.
     *
     * @param exportType The C pointer to the `wasm_exporttype_t` struct.
     *
     * @throws Error If there is a failure to get the export name or type from the C API.
     *
     * @note takes ownership of the wasm_exporttype_t and immediately deletes it.
     */
    constructor(exportType: CPointer<wasm_exporttype_t>) : this(
        exportType.let {
            val namePtr = wasm_exporttype_name(exportType) ?: throw Error("Failed to get export name")
            namePtr.pointed.data!!.toKString()
        },
        exportType.let {
            val externTypePtr = wasm_exporttype_type(exportType) ?: throw Error("Failed to get export type")
            ExternType(externTypePtr)
        }
    ) {
        wasm_exporttype_delete(exportType)
    }
}