package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

/**
 * A Kotlin/Native wrapper for the `wasm_importtype_t` struct, representing the type of a WebAssembly import.
 *
 * @property module The module name from which the import is being imported.
 * @property name The name of the import within the module.
 * @property type The [ExternType] of the imported item.
 * @constructor Creates a new [ImportType] instance from the given C pointer.
 *
 * @note The `module`, `name`, and `externType` properties should not be manually deallocated as
 * they are owned by the `wasm_importtype_t` struct.
 */
class ImportType(
    val module: String,
    val name: String,
    val type: ExternType
) {

    /**
     * Creates a new [ImportType] instance from the given C pointer to a `wasm_importtype_t` struct.
     *
     * @param importType The C pointer to the `wasm_importtype_t` struct.
     *
     * @note takes ownership of the wasm_importtype_t and immediately deletes it.
     */
    constructor(importType: CPointer<wasm_importtype_t>) : this(
        importType.let {
            val moduleNamePtr = wasm_importtype_module(importType) ?: throw Error("Failed to get module name")
            moduleNamePtr.pointed.data!!.toKString()
                       },
        importType.let {
            val namePtr = wasm_importtype_name(importType) ?: throw Error("Failed to get import name")
            namePtr.pointed.data!!.toKString()
                       },
        importType.let {
            val externTypePtr = wasm_importtype_type(importType) ?: throw Error("Failed to get import type")
            ExternType.fromCValue(externTypePtr)
        }
    ) {
        wasm_importtype_delete(importType)
    }
}
