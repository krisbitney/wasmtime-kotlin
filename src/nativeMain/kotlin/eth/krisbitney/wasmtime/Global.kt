package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.wasm.GlobalType
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class Global(
    store: CPointer<wasmtime_context_t>,
    val global: CPointer<wasmtime_global_t>
) : Extern(store, Extern.Kind.GLOBAL), AutoCloseable {

    val type: GlobalType by lazy {
        val ptr = wasmtime_global_type(store, global) ?: throw Exception("Failed to get global type")
        GlobalType(ptr)
    }

    constructor(
        store: Store<*>,
        globalType: GlobalType,
        value: Val
    ): this(
        store.context.context,
        nativeHeap.alloc<wasmtime_global_t>().apply {
            val cGlobalType = GlobalType.allocateCValue(globalType)
            val wasmtimeVal = Val.allocateCValue(value)
            val error = wasmtime_global_new(
                store.context.context,
                cGlobalType,
                wasmtimeVal,
                this.ptr
            )
            GlobalType.deleteCValue(cGlobalType)
            Val.deleteCValue(wasmtimeVal)

            if (error != null) {
                nativeHeap.free(this)
                throw WasmtimeException(error)
            }
        }.ptr
    )

    fun get(): Val {
        val valuePtr = nativeHeap.alloc<wasmtime_val_t>()
        wasmtime_global_get(store, global, valuePtr.ptr)
        val result = Val.fromCValue(valuePtr.ptr)
        Val.deleteCValue(valuePtr.ptr)
        return result
    }

    fun set(value: Val) {
        val wasmtimeVal = Val.allocateCValue(value)
        val error = wasmtime_global_set(store, global, wasmtimeVal)
        Val.deleteCValue(wasmtimeVal)
        if (error != null) {
            throw WasmtimeException(error)
        }
    }

    override fun close() {
        nativeHeap.free(global)
    }
}
