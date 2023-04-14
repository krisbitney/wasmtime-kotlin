package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import wasmtime.*

/** never owned; does not need to be deleted */
class Caller(val caller: CPointer<wasmtime_caller_t>) {

    val context: Context<Any?>
        get() {
            val ptr = wasmtime_caller_context(caller) ?: throw Exception("failed to get caller context")
            return Context(ptr)
        }

    /** The returned Extern is owned by the caller */
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