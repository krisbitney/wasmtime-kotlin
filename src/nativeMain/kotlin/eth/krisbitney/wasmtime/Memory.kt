package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t
import eth.krisbitney.wasmtime.wasm.MemoryType
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class Memory(
    store: CPointer<wasmtime_context_t>,
    val memory: CPointer<wasmtime_memory_t>
) : Extern(store, Extern.Kind.MEMORY), AutoCloseable {

    val type: MemoryType by lazy {
        val memoryType = wasmtime_memory_type(store, memory) ?: throw Exception("failed to get memory type")
        MemoryType(memoryType)
    }

    val data: ByteArray get() {
        val data = wasmtime_memory_data(store, memory) ?: throw Exception("failed to get memory data")
        return data.readBytes(dataSize.convert())
    }

    val dataSize: size_t get() = wasmtime_memory_data_size(store, memory)

    val size: ULong get() = wasmtime_memory_size(store, memory)

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
