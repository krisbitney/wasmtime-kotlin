package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import wasmtime.*

/**
 * Represents a caller of a host-defined function.
 *
 * @property caller A pointer to the underlying [wasmtime_caller_t] structure.
 */
class Caller(val caller: CPointer<wasmtime_caller_t>) {

    /**
     * Retrieves the context associated with this caller.
     * The returned context can be used to interact with objects in the caller's store.
     *
     * @throws Exception if the context retrieval fails.
     */
    val context: Context<Any?>
        get() {
            val ptr = wasmtime_caller_context(caller) ?: throw Exception("failed to get caller context")
            return Context(ptr)
        }

    /**
     * Retrieves the exported WebAssembly value by its name from the caller's context.
     *
     * @param name The name of the exported value to look up.
     * @return An [Extern] instance if the exported value is found, or `null` if not found.
     *
     * @note Currently, this function only works for exported memories due to WASI compatibility.
     */
    fun exportGet(name: String): Extern? = memScoped {
        val wasmExtern = alloc<wasmtime_extern_t>()
        val found =  wasmtime_caller_export_get(caller, name, name.length.toULong(), wasmExtern.ptr)
        return if (found) {
            Extern.fromCValue(context.context, wasmExtern.ptr)
        } else {
            null
        }
    }
}