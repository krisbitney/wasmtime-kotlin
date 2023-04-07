package wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t

@OptIn(ExperimentalStdlibApi::class)
class ExternRef<T>(val externRef: CPointer<wasmtime_externref_t>) : AutoCloseable {

    constructor(context: Context<*>, raw: size_t) :
            this(wasmtime_externref_from_raw(context.context, raw) ?: throw Error("failed to create ExternRef from raw"))

    constructor(data: COpaquePointer?, finalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>>?) :
            this(wasmtime_externref_new(data, finalizer) ?: throw Error("failed to create ExternRef"))

    constructor(data: T) :
            this(
                if (data != null) {
                    val dataPtr: COpaquePointer = StableRef.create(data).asCPointer()
                    val finalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>> = staticCFunction { ptr: COpaquePointer? ->
                        ptr?.asStableRef<Any>()?.dispose()
                    }
                    wasmtime_externref_new(dataPtr, finalizer) ?: throw Error("failed to create ExternRef")
                } else {
                    wasmtime_externref_new(null, null) ?: throw Error("failed to create ExternRef")
                }
            )

    inline fun <reified T: Any> data(): T? {
        return wasmtime_externref_data(externRef)?.asStableRef<T>()?.get()
    }

    fun clone(): ExternRef<T> {
        val clonedRef = wasmtime_externref_clone(externRef) ?: throw Error("failed to clone ExternRef")
        return ExternRef(clonedRef)
    }

    fun toRaw(context: Context<*>): size_t {
        return wasmtime_externref_to_raw(context.context, externRef)
    }

    companion object {
        inline fun <reified T>fromRaw(context: Context<*>, raw: size_t): ExternRef<T> {
            val externRef = wasmtime_externref_from_raw(context.context, raw) ?: throw Error("failed to create ExternRef from raw")
            return ExternRef(externRef)
        }
    }

    override fun close() {
        wasmtime_externref_delete(externRef)
    }
}
