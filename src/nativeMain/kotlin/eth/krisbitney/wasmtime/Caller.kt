package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import wasmtime.*

/** never owned; does not need to be deleted */
class Caller(val caller: CPointer<wasmtime_caller_t>) {

    val context: Context<Any?> = Context(
        wasmtime_caller_context(caller)
            ?: throw Exception("failed to get caller context")
    )

    /** The returned Extern is owned by the caller */
    fun exportGet(name: String): Extern? {
        val wasmExtern = nativeHeap.alloc<wasmtime_extern_t>()
        val found =  wasmtime_caller_export_get(caller, name, name.length.toULong(), wasmExtern.ptr)
        var extern: Extern? = null
        if (found) {
            extern = Extern.fromCValue(context.context, wasmExtern.ptr)
        }
        Extern.deleteCValue(wasmExtern.ptr)
        return extern
    }
}