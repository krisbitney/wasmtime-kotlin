package io.github.krisbitney.wasmtime

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
     * Unlike other methods that return an [Extern], here the caller owns the returned value
     * and must manually free the C value contained in it using [freeExtern].
     * For example: `caller.freeExtern(memory)`
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

    /**
     * Free the memory associated with an [Extern] returned by [exportGet].
     * Calling this function more than once on the same [Extern] will crash the process
     * Attempting to use the [Extern] after calling this function will crash the process.
     *
     * @param extern The [Extern] to free.
     */
    fun freeExtern(extern: Extern) {
        when (extern) {
            is Func -> nativeHeap.free(extern.func)
            is Global -> nativeHeap.free(extern.global)
            is Table -> nativeHeap.free(extern.table)
            is Memory -> nativeHeap.free(extern.memory)
        }
    }
}