package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import wasmtime.*

/**
 * A [Store] is responsible for the storage and management of WebAssembly objects.
 * Stores are cheap to create and cheap to dispose.
 * It is safe to move a [Store] to any thread at any time, but a [Store] generally cannot be used concurrently.
 *
 * @param T The type of data to be associated with the [Store].
 * @property engine The [Engine] instance that the [Store] is connected to.
 * @property data Optional user-provided data that will be associated with the [Store].
 *
 * @constructor Creates a new [Store] instance.
 * @throws RuntimeException if the Wasmtime store creation fails.
 */
@OptIn(ExperimentalStdlibApi::class)
class Store<T>(val engine: Engine, initData: T? = null) : AutoCloseable {

    private val store: CPointer<wasmtime_store_t>
    private val owned: MutableList<COpaquePointer> = mutableListOf()

    /**
     * The user-provided data associated with the [Store].
     */
    var data: T? = initData
        set(value) {
            this.context.setData(value)
            field = value
        }

    init {
        val dataPtr: COpaquePointer? = this.data?.let { StableRef.create(it).asCPointer() }
        val finalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>> = staticCFunction { ptr: COpaquePointer? ->
            ptr?.asStableRef<Any>()?.dispose()
        }
        store = wasmtime_store_new(engine.engine, dataPtr, finalizer)
            ?: throw RuntimeException("Failed to create Wasmtime store")
    }

    /**
     * Retrieves the [Context] associated with this [Store].
     * @throws RuntimeException if the Wasmtime context retrieval fails.
     */
    val context: Context<T>
        get() {
            val ptr = wasmtime_store_context(store) ?: throw RuntimeException("Failed to get Wasmtime context")
            return Context(ptr)
        }

    /**
     * Sets limits for the [Store] to control the consumption of resources by instances.
     * These limits are used to prevent instances from over-consuming resources.
     * Use a negative value for any parameter that should keep the default value.
     *
     * @param memorySize The maximum number of bytes a linear memory can grow to. Default is unlimited.
     * @param tableElements The maximum number of elements in a table. Default is unlimited.
     * @param instances The maximum number of instances that can be created for a [Store]. Default is 10,000.
     * @param tables The maximum number of tables that can be created for a [Store]. Default is 10,000.
     * @param memories The maximum number of linear memories that can be created for a [Store]. Default is 10,000.
     */
    fun setLimiter(
        memorySize: Long,
        tableElements: Long,
        instances: Long,
        tables: Long,
        memories: Long
    ) {
        wasmtime_store_limiter(store, memorySize, tableElements, instances, tables, memories)
    }

    fun own(ptr: COpaquePointer) = owned.add(ptr)

    override fun close() {
        wasmtime_store_delete(store)
        owned.forEach { nativeHeap.free(it) }
    }
}

