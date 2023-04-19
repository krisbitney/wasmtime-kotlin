package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.wasm.GlobalType
import wasmtime.*

/**
 * Represents a WebAssembly global value in the context of a given store.
 *
 * A Global is a container for a WebAssembly global value and is associated with a [Store] and [GlobalType].
 * Global values can be mutable or immutable and can be read and updated (if mutable) through the provided methods.
 *
 * @property store The [CPointer] to the [wasmtime_context_t] that represents the store containing this global.
 * @property global The [CPointer] to the [wasmtime_global_t] that represents the global value.
 */
@OptIn(ExperimentalStdlibApi::class)
class Global(
    store: CPointer<wasmtime_context_t>,
    val global: CPointer<wasmtime_global_t>
) : Extern(store, Extern.Kind.GLOBAL), AutoCloseable {

    /**
     * The [GlobalType] of this global value.
     */
    val type: GlobalType by lazy {
        val ptr = wasmtime_global_type(this.store, global) ?: throw Exception("Failed to get global type")
        GlobalType(ptr)
    }

    /**
     * Creates a new host-defined global value within the provided store.
     *
     * @param store The [Store] in which to create the global.
     * @param globalType The [GlobalType] specifying the WebAssembly type of the global being created.
     * @param value The initial value of the global, specified as a [Val].
     * @throws WasmtimeException If the provided value does not match the specified type of the global or comes from a different store.
     */
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

    /**
     * Gets the current value of the global.
     *
     * @return The current value of the global as a [Val] instance.
     */
    fun get(): Val {
        val valuePtr = nativeHeap.alloc<wasmtime_val_t>()
        wasmtime_global_get(store, global, valuePtr.ptr)
        val result = Val.fromCValue(valuePtr.ptr)
        Val.deleteCValue(valuePtr.ptr)
        return result
    }

    /**
     * Sets the global to a new value.
     *
     * @param value The new value to store in the global as a [Val] instance.
     * @throws WasmtimeException If the global is not mutable or if the provided value has the wrong type for the global.
     */
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
