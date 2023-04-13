package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.wasm.GlobalType
import eth.krisbitney.wasmtime.wasm.Mutability
import eth.krisbitney.wasmtime.wasm.ValType
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class Global(
    private val store: CPointer<wasmtime_context_t>,
    val global: CPointer<wasmtime_global_t>
) : AutoCloseable {

    constructor(
        store: Store<*>,
        kind: ValType.Kind,
        mutability: Mutability,
        value: Val
    ): this(
        store.context.context,
        nativeHeap.alloc<wasmtime_global_t>().apply {
            val globalType = GlobalType(
                kind,
                mutability
            )
            val wasmtimeVal = Val.allocateCValue(value)
            val error = wasmtime_global_new(
                store.context.context,
                globalType.globalType,
                wasmtimeVal,
                this.ptr
            )
            globalType.close()
            Val.deleteCValue(wasmtimeVal)

            if (error != null) {
                nativeHeap.free(this)
                throw WasmtimeException(error)
            }
        }.ptr
    )

    fun type(): GlobalType {
        val ptr = wasmtime_global_type(store, global) ?: throw Exception("Failed to get global type")
        return GlobalType(ptr)
    }

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
