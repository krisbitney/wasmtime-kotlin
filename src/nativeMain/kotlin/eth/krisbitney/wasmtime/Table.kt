package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.wasm.TableType
import wasmtime.*

/**
 * Represents a WebAssembly table. Elements must be of type [ExternRef] or [FuncRef].
 *
 * @property store A pointer to the wasmtime context used for table operations.
 * @property table A pointer to the wasmtime table.
 */
class Table(
    store: CPointer<wasmtime_context_t>,
    val table: CPointer<wasmtime_table_t>,
) : Extern(store, Extern.Kind.TABLE) {

    /**
     * Retrieves the [TableType] of the table.
     */
    val type: TableType by lazy {
        val ptr = wasmtime_table_type(this.store, table) ?: throw Exception("failed to get table type")
        TableType(ptr)
    }

    /**
     * Retrieves the size of the table in elements.
     */
    val size: UInt get() = wasmtime_table_size(this.store, table)

    /**
     * Constructs a new table with the specified [store], initial value [init], and [tableType].
     * The initial value will be copied into each element of the table.
     * Valid [Val] types are [Val.Kind.EXTERNREF] and [Val.Kind.FUNCREF].
     *
     * @param store The [Store] to create the table within.
     * @param init The initial value for the table's elements.
     * @param tableType The [TableType] of the table to create.
     */
    constructor(
        store: Store<*>,
        init: Val,
        tableType: TableType
    ): this(store.context.context,
        store.run {
            if (init.kind != Val.Kind.EXTERNREF && init.kind != Val.Kind.FUNCREF) {
                throw IllegalArgumentException("Table elements must be either ExternRef or FuncRef")
            }

            val cTable = nativeHeap.alloc<wasmtime_table_t>()
            val cTableType = TableType.allocateCValue(tableType)
            val cVal = Val.allocateCValue(init)
            val error = wasmtime_table_new(store.context.context, cTableType, cVal, cTable.ptr)
            TableType.deleteCValue(cTableType)
            Val.deleteCValue(cVal)
            error?.let {
                nativeHeap.free(cTable.ptr)
                throw WasmtimeException(it)
            }
            store.own(cTable.ptr)
            cTable.ptr
        }
    )

    /**
     * Retrieves the value at the specified [index] in the table.
     *
     * @param index The table index to access.
     * @return The [Val] at the specified [index] or `null` if the index is out-of-bounds.
     */
    fun get(index: UInt): Val? = memScoped {
        val wasmtimeVal = alloc<wasmtime_val_t>()
        val success = wasmtime_table_get(store, table, index, wasmtimeVal.ptr)
        return if (success) {
            Val.fromCValue(wasmtimeVal.ptr)
        } else {
            null
        }
    }

    /**
     * Sets the value at the specified [index] in the table to the provided [value].
     *
     * @param index The table index to write.
     * @param value The [Val] to store.
     * @throws [WasmtimeException] If the value has the wrong type for the table or the index is out of bounds.
     */
    fun set(index: UInt, value: Val) {
        val cVal = Val.allocateCValue(value)
        val error = wasmtime_table_set(store, table, index, cVal)
        Val.deleteCValue(cVal)
        error?.let { throw WasmtimeException(it, true) }
    }

/**
 * Grows the table by the specified [delta] number of elements with the initial value [init].
 *
 * @param delta The number of elements to grow the table by.
 * @param init The initial value for new table element slots.
 * @return The previous size of the table before growth.
 * @throws [WasmtimeException] If the value has the wrong type for the table.
 * @throws [WasmtimeException] If the table's maximum size would be exceeded.
 */
    fun grow(delta: UInt, init: Val): UInt = memScoped {
        val prevSize = alloc<UIntVar>()
        val cVal = Val.allocateCValue(init)
        val error = wasmtime_table_grow(store, table, delta, cVal, prevSize.ptr)
        Val.deleteCValue(cVal)
        error?.let { throw WasmtimeException(it) }
        prevSize.value
    }
}