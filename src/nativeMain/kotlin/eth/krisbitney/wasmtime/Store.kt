package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class Store<T>(
    val engine: Engine,
    val data: T? = null
) : AutoCloseable {
    private val store: CPointer<wasmtime_store_t>

    init {
        val dataPtr: COpaquePointer? = data?.let { StableRef.create(it).asCPointer() }
        val finalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>> = staticCFunction { ptr: COpaquePointer? ->
            ptr?.asStableRef<Any>()?.dispose()
        }
        store = wasmtime_store_new(engine.engine, dataPtr, finalizer)
            ?: throw RuntimeException("Failed to create Wasmtime store")
    }

    val context: Context<T?>
        get() {
            val ptr = wasmtime_store_context(store) ?: throw RuntimeException("Failed to get Wasmtime context")
            return Context(ptr)
        }

    fun setLimiter(
        memorySize: Long,
        tableElements: Long,
        instances: Long,
        tables: Long,
        memories: Long
    ) {
        wasmtime_store_limiter(store, memorySize, tableElements, instances, tables, memories)
    }

    override fun close() {
        wasmtime_store_delete(store)
    }
}

