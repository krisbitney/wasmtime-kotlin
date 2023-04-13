package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

/**
 * Represents the type of a WebAssembly table, containing information about the
 * element type and the limits of the table.
 *
 * @property element The [ValType.Kind] describing the element type of the table.
 * @property limits The [WasmLimits] describing the minimum and maximum size of the table.
 *
 * @constructor Constructs a new [TableType] instance from a C pointer to a `wasm_tabletype_t`.
 * @param tableType The C pointer to the `wasm_tabletype_t`.
 * @throws RuntimeException If the element type retrieval fails.
 * @throws Error If the table type limits retrieval fails.
 */
class TableType(
    val element: ValType.Kind,
    val limits: WasmLimits
) : ExternType(ExternType.Kind.TABLE) {

    constructor(tableType: CPointer<wasm_tabletype_t>) : this(
        wasm_tabletype_element(tableType)?.let {
            ValType.kindFromCValue(it)
        } ?: throw RuntimeException("failed to get table type element"),
        wasm_tabletype_limits(tableType)?.let {
            WasmLimits(it.pointed.min, it.pointed.max)
        } ?: throw Error("failed to get table type limits")
    ) {
        wasm_tabletype_delete(tableType)
    }

    /**
     * Companion object providing utility methods for working with C values and pointers
     * related to the [TableType] class.
     */
    companion object {
        /**
         * Allocates a new C pointer for the given [TableType] and creates a `wasm_tabletype_t` instance.
         *
         * @param tableType The [TableType] to be used for creating the `wasm_tabletype_t`.
         * @return The newly created C pointer to a `wasm_tabletype_t`.
         * @throws Error If there is a failure to create the table type.
         */
        fun allocateCValue(tableType: TableType): CPointer<wasm_tabletype_t> {
            val cElement = ValType.allocateCValue(tableType.element)
            val cLimits = WasmLimits.allocateCValue(tableType.limits.min, tableType.limits.max).ptr
            val cTableType = wasm_tabletype_new(cElement, cLimits)
            nativeHeap.free(cLimits)
            if (cTableType == null) {
                nativeHeap.free(cElement)
                throw Error("failed to create table type")
            }
            return cTableType
        }

        /**
         * Deletes the C value for the given `wasm_tabletype_t` pointer.
         *
         * @param tableType The C pointer to the `wasm_tabletype_t` to be deleted.
         */
        fun deleteCValue(tableType: CPointer<wasm_tabletype_t>) {
            wasm_tabletype_delete(tableType)
        }
    }
}
