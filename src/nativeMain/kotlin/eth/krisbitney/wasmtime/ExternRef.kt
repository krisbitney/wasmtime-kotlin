package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t
import wasmtime.*

/**
 * Wraps the Wasmtime `wasmtime_externref_t` C API and provides a Kotlin-native API to manage
 * un-forgeable WebAssembly extern references. The wrapped references are guaranteed to be created by
 * the host, and they can't be forged by WebAssembly code itself.
 *
 * @param T The type of the wrapped data.
 * @param externRef The C pointer to the `wasmtime_externref_t` struct representing the extern reference.
 * @constructor Creates an [ExternRef] instance from the given [externRef] C pointer.
 */
@OptIn(ExperimentalStdlibApi::class)
class ExternRef<T>(val externRef: CPointer<wasmtime_externref_t>) : AutoCloseable {

    /**
     * Creates an [ExternRef] instance from a raw value and a [Context].
     *
     * @param context A [Context] instance to be used to create the extern reference.
     * @param raw A raw value of type [size_t] representing the extern reference.
     * @throws Exception If the creation of the [ExternRef] instance fails.
     */
    constructor(context: Context<*>, raw: size_t) :
            this(wasmtime_externref_from_raw(context.context, raw) ?: throw Exception("failed to create ExternRef from raw"))

    /**
     * Creates an [ExternRef] instance from a nullable [data] pointer and an optional [finalizer].
     *
     * @param data A nullable [COpaquePointer] representing the data to be wrapped by the extern reference.
     * @param finalizer An optional [CPointer] to a finalizer function for the wrapped data.
     * @throws Exception If the creation of the [ExternRef] instance fails.
     */
    constructor(data: COpaquePointer?, finalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>>?) :
            this(wasmtime_externref_new(data, finalizer) ?: throw Exception("failed to create ExternRef"))

    /**
     * Creates an [ExternRef] instance from a nullable [data] of type [T].
     *
     * @param data A nullable instance of type [T] to be wrapped by the extern reference.
     * @throws Exception If the creation of the [ExternRef] instance fails.
     */
    constructor(data: T) :
            this(
                if (data != null) {
                    val dataPtr: COpaquePointer = StableRef.create(data).asCPointer()
                    val finalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>> = staticCFunction { ptr: COpaquePointer? ->
                        ptr?.asStableRef<Any>()?.dispose()
                    }
                    wasmtime_externref_new(dataPtr, finalizer) ?: throw Exception("failed to create ExternRef")
                } else {
                    wasmtime_externref_new(null, null) ?: throw Exception("failed to create ExternRef")
                }
            )

    /**
     * Retrieves the wrapped data of type [T] from the extern reference.
     *
     * @return A nullable instance of type [T] representing the wrapped data.
     */
    inline fun <reified T: Any> data(): T? {
        val ref = wasmtime_externref_data(externRef)?.asStableRef<T>()
        val data = ref?.get()
        ref?.dispose()
        return data
    }

    /**
     * Clones the extern reference, creating a new [ExternRef] instance with the same wrapped data.
     *
     * @return A new [ExternRef] instance representing the cloned extern reference.
     * @throws Exception If the cloning of the extern reference fails.
     */
    fun clone(): ExternRef<T> {
        val clonedRef = wasmtime_externref_clone(externRef) ?: throw Exception("failed to clone ExternRef")
        return ExternRef(clonedRef)
    }

    /**
     * Converts the extern reference to a raw value suitable for use in a [Context].
     *
     * @param context A [Context] instance to be used to convert the extern reference to a raw value.
     * @return A [size_t] value representing the raw extern reference.
     */
    fun toRaw(context: Context<*>): size_t {
        return wasmtime_externref_to_raw(context.context, externRef)
    }

    /**
     * A companion object providing utility functions to create and manage [ExternRef] instances.
     */
    companion object {
    /**
     * Creates an [ExternRef] instance of type [T] from a raw value and a [Context].
     *
     * @param context A [Context] instance to be used to create the extern reference.
     * @param raw A raw value of type [size_t] representing the extern reference.
     * @return A new [ExternRef] instance representing the created extern reference.
     */
        inline fun <reified T>fromRaw(context: Context<*>, raw: size_t): ExternRef<T> {
            val externRef = wasmtime_externref_from_raw(context.context, raw) ?: throw Exception("failed to create ExternRef from raw")
            return ExternRef(externRef)
        }
    }

    override fun close() {
        wasmtime_externref_delete(externRef)
    }
}
