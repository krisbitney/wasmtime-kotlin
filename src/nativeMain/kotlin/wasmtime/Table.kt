package wasmtime

import kotlinx.cinterop.*
import wasm.WasmLimits
import wasm.WasmTableType
import wasm.WasmValType

@OptIn(ExperimentalStdlibApi::class)
class Table(
    private val store: CPointer<wasmtime_context_t>,
    val table: CPointer<wasmtime_table_t>,
) : AutoCloseable {

    constructor(
        store: Store<*>,
        init: Val,
        elementType: WasmValType.WasmValKind,
        min: UInt = 0u,
        max: UInt = WasmLimits.LIMITS_MAX_DEFAULT,
    ): this(store.context.context,
        nativeHeap.alloc<wasmtime_table_t>().apply {
            val valType = WasmValType(elementType)
            val tableType = WasmTableType(valType, min, max)
            val error = wasmtime_table_new(store.context.context, tableType.tableType, init.wasmtimeVal, this.ptr)
            error?.let {
                tableType.close()
                nativeHeap.free(this)
                throw WasmtimeError(it)
            }
        }.ptr
    )

    val type: WasmTableType get() = WasmTableType(wasmtime_table_type(store, table) ?: throw Error("failed to get table type"))

    fun get(index: UInt): Val? = nativeHeap.alloc<wasmtime_val_t>().run {
        val success = wasmtime_table_get(store, table, index, this.ptr)
        return if (success) {
            Val(this.ptr)
        } else {
            nativeHeap.free(this)
            null
        }
    }

    fun set(index: UInt, value: Val) {
        val error = wasmtime_table_set(store, table, index, value.wasmtimeVal)
        error?.let { throw WasmtimeError(it) }
    }

    val size: UInt get() = wasmtime_table_size(store, table)

    fun grow(delta: UInt, init: wasmtime_val_t): UInt = memScoped {
        val prevSize = alloc<UIntVar>()
        val error = wasmtime_table_grow(store, table, delta, init.ptr, prevSize.ptr)
        error?.let { throw WasmtimeError(it) }
        prevSize.value
    }

    override fun close() {
        type.close()
        nativeHeap.free(table)
    }
}