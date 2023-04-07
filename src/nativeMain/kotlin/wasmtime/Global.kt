package wasmtime

import kotlinx.cinterop.*
import wasm.WasmGlobalType
import wasm.WasmMutability
import wasm.WasmValType

@OptIn(ExperimentalStdlibApi::class)
class Global(
    private val store: CPointer<wasmtime_context_t>,
    val global: CPointer<wasmtime_global_t>
) : AutoCloseable {

    constructor(
        store: Store<*>,
        kind: WasmValType.WasmValKind,
        mutability: WasmMutability,
        value: Val
    ): this(
        store.context.context,
        nativeHeap.alloc<wasmtime_global_t>().apply {
            val globalType = WasmGlobalType(
                WasmValType(kind),
                mutability
            )
            val error = wasmtime_global_new(
                store.context.context,
                globalType.globalType,
                value.wasmtimeVal,
                this.ptr
            )

            if (error != null) {
                globalType.close()
                nativeHeap.free(this)
                throw WasmtimeError(error)
            }
        }.ptr
    )

    fun type(): WasmGlobalType {
        val ptr = wasmtime_global_type(store, global) ?: throw Exception("Failed to get global type")
        return WasmGlobalType(ptr)
    }

    fun get(): Val {
        val valuePtr = nativeHeap.alloc<wasmtime_val_t>()
        wasmtime_global_get(store, global, valuePtr.ptr)
        return Val(valuePtr.ptr)
    }

    fun set(value: Val) {
        val error = wasmtime_global_set(store, global, value.wasmtimeVal)
        if (error != null) {
            throw WasmtimeError(error)
        }
    }

    override fun close() {
        type().close()
        nativeHeap.free(global)
    }
}
