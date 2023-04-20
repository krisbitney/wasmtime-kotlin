package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t
import eth.krisbitney.wasmtime.wasm.MemoryType
import wasmtime.*

/**
 * Represents a WebAssembly linear memory object in a [Store].
 *
 * This class provides methods to interact with WebAssembly memory objects, such as reading and writing
 * data, querying memory type, and resizing the memory.
 *
 * @property store A [CPointer] to a [wasmtime_context_t] representing the store containing this memory object.
 * @property memory A [CPointer] to a [wasmtime_memory_t] representing the WebAssembly memory object.
 */
@OptIn(ExperimentalStdlibApi::class)
class Memory(
    store: CPointer<wasmtime_context_t>,
    val memory: CPointer<wasmtime_memory_t>
) : Extern(store, Extern.Kind.MEMORY), AutoCloseable {

    /**
     * Retrieves the [MemoryType] of this memory object.
     */
    val type: MemoryType by lazy {
        val memoryType = wasmtime_memory_type(this.store, memory) ?: throw Exception("failed to get memory type")
        MemoryType(memoryType)
    }

    /**
     * Retrieves the memory object's data as a [ByteArray].
     */
    val data: ByteArray get() {
        val data = wasmtime_memory_data(store, memory) ?: throw Exception("failed to get memory data")
        return data.readBytes(dataSize.convert())
    }

    /**
     * Retrieves the byte length of this linear memory.
     */
    val dataSize: size_t get() = wasmtime_memory_data_size(store, memory)

    /**
     * Retrieves the length of this linear memory in WebAssembly pages of 64 KiB.
     */
    val size: ULong get() = wasmtime_memory_size(store, memory)

    /**
     * Creates a new memory object within the provided [store] and the specified [memoryType].
     *
     * @param store A [Store] in which to create the memory object.
     * @param memoryType A [MemoryType] representing the type of memory to be created.
     */
    constructor(store: Store<*>, memoryType: MemoryType) :
            this(
                store.context.context,
                nativeHeap.alloc<wasmtime_memory_t>().apply {
                    val cMemoryType = MemoryType.allocateCValue(memoryType)
                    val error = wasmtime_memory_new(store.context.context, cMemoryType, this.ptr)
                    MemoryType.deleteCValue(cMemoryType)
                    if (error != null) {
                        nativeHeap.free(this)
                        throw WasmtimeException(error)
                    }
                }.ptr
            )

    /**
     * Grows the memory by the specified [delta] number of pages.
     * A WebAssembly page is 64 KiB in size.
     *
     * @param delta The number of pages to grow the memory by.
     * @return The previous size of the memory, in WebAssembly pages.
     * @throws [WasmtimeException] If the memory cannot be grown.
     */
    fun grow(delta: ULong): ULong = memScoped {
        val prevSize = alloc<ULongVar>()
        val error = wasmtime_memory_grow(store, memory, delta, prevSize.ptr)
        if (error != null) throw WasmtimeException(error)
        return prevSize.value
    }

    override fun close() {
        nativeHeap.free(memory)
    }
}
