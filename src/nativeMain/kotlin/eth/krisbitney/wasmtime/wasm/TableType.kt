package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

/**
 * A Kotlin/Native wrapper for the `wasm_tabletype_t` struct, representing the type of a WebAssembly table.
 *
 * @property tableType The C pointer to the `wasm_tabletype_t` struct.
 * @constructor Creates a new [TableType] instance from the given C pointer.
 * @constructor Creates a new [TableType] instance with the specified element type, minimum, and maximum table limits.
 */
@OptIn(ExperimentalStdlibApi::class)
class TableType(val tableType: CPointer<wasm_tabletype_t>) : AutoCloseable {

    /**
     * Creates a new [TableType] instance with the specified element type, minimum, and maximum table limits.
     *
     * @param elementType The element type of the table.
     * @param min The minimum number of elements in the table.
     * @param max The maximum number of elements in the table.
     */
    constructor(elementType: ValType.Kind, min: UInt = 0u, max: UInt = WasmLimits.LIMITS_MAX_DEFAULT) :
            this(elementType.run {
                val limits = WasmLimits.cLimits(min, max).ptr
                val tableType = wasm_tabletype_new(ValType.allocateCValue(elementType), limits)
                    ?: throw Error("failed to create table type")
                nativeHeap.free(limits)
                tableType
            })

    /**
     * Retrieves the element type of the WebAssembly table as a [ValType.Kind].
     *
     * @return The element type of the table.
     */
    val element: ValType.Kind
        get() {
            val result = wasm_tabletype_element(tableType)  ?: throw RuntimeException("failed to get table type element")
            return ValType.kindFromCValue(result)
        }

    /**
     * Retrieves the limits of the WebAssembly table as a [WasmLimits] instance.
     *
     * @return The limits of the table.
     */
    val limits: WasmLimits
        get() {
            val ptr = wasm_tabletype_limits(tableType) ?: throw Error("failed to get table type limits")
            return WasmLimits(ptr.pointed.min, ptr.pointed.max)
        }

    override fun close() {
        wasm_tabletype_delete(tableType)
    }
}
