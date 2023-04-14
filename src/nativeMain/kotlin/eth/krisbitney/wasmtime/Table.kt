package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.wasm.TableType
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class Table(
    store: CPointer<wasmtime_context_t>,
    val table: CPointer<wasmtime_table_t>,
) : Extern(store, Extern.Kind.TABLE), AutoCloseable {

    constructor(
        store: Store<*>,
        init: Val,
        tableType: TableType
    ): this(store.context.context,
        nativeHeap.alloc<wasmtime_table_t>().apply {
            val cTableType = TableType.allocateCValue(tableType)
            val wasmtimeVal = Val.allocateCValue(init)
            val error = wasmtime_table_new(store.context.context, cTableType, wasmtimeVal, this.ptr)
            TableType.deleteCValue(cTableType)
            Val.deleteCValue(wasmtimeVal)
            error?.let {
                nativeHeap.free(this)
                throw WasmtimeException(it)
            }
        }.ptr
    )

    val type: TableType get() = TableType(wasmtime_table_type(store, table) ?: throw Error("failed to get table type"))

    fun get(index: UInt): Val? = memScoped {
        val wasmtimeVal = alloc<wasmtime_val_t>()
        val success = wasmtime_table_get(store, table, index, wasmtimeVal.ptr)
        return if (success) {
            Val.fromCValue(wasmtimeVal.ptr)
        } else {
            null
        }
    }

    fun set(index: UInt, value: Val) {
        val wasmtimeVal = Val.allocateCValue(value)
        val error = wasmtime_table_set(store, table, index, wasmtimeVal)
        Val.deleteCValue(wasmtimeVal)
        error?.let { throw WasmtimeException(it) }
    }

    val size: UInt get() = wasmtime_table_size(store, table)

    fun grow(delta: UInt, init: wasmtime_val_t): UInt = memScoped {
        val prevSize = alloc<UIntVar>()
        val error = wasmtime_table_grow(store, table, delta, init.ptr, prevSize.ptr)
        error?.let { throw WasmtimeException(it) }
        prevSize.value
    }

    override fun close() {
        nativeHeap.free(table)
    }
}