package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.wasm.WasmLimits
import eth.krisbitney.wasmtime.wasm.TableType
import eth.krisbitney.wasmtime.wasm.ValType
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class Table(
    private val store: CPointer<wasmtime_context_t>,
    val table: CPointer<wasmtime_table_t>,
) : AutoCloseable {

    constructor(
        store: Store<*>,
        init: Val,
        elementType: ValType.Kind,
        min: UInt = 0u,
        max: UInt = WasmLimits.LIMITS_MAX_DEFAULT,
    ): this(store.context.context,
        nativeHeap.alloc<wasmtime_table_t>().apply {
            val tableType = TableType(elementType, min, max)
            val wasmtimeVal = Val.allocateCValue(init)
            val error = wasmtime_table_new(store.context.context, tableType.tableType, wasmtimeVal, this.ptr)
            tableType.close()
            Val.deleteCValue(wasmtimeVal)
            error?.let {
                tableType.close()
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