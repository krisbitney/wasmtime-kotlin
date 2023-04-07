package wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t
import wasm.WasmLimits
import wasm.WasmMemoryType

@OptIn(ExperimentalStdlibApi::class)
class Memory(
    private val store: CPointer<wasmtime_context_t>,
    val memory: CPointer<wasmtime_memory_t>
) : AutoCloseable {

    constructor(store: Store<*>, min: UInt = 0u, max: UInt = WasmLimits.LIMITS_MAX_DEFAULT) :
            this(
                store.context.context,
                nativeHeap.alloc<wasmtime_memory_t>().apply {
                    val memoryType = WasmMemoryType(min, max)
                    val error = wasmtime_memory_new(store.context.context, memoryType.memoryType, this.ptr)
                    if (error != null) {
                        memoryType.close()
                        nativeHeap.free(this)
                        throw WasmtimeError(error)
                    }
                }.ptr
            )

    val type: WasmMemoryType get() {
        val memoryType = wasmtime_memory_type(store, memory) ?: throw Error("failed to get memory type")
        return WasmMemoryType(memoryType)
    }

    val data: ByteArray get() {
        val data = wasmtime_memory_data(store, memory) ?: throw Error("failed to get memory data")
        return data.readBytes(dataSize.convert())
    }

    val dataSize: size_t get() = wasmtime_memory_data_size(store, memory)

    val size: ULong get() = wasmtime_memory_size(store, memory)

    fun grow(delta: ULong): ULong = memScoped {
        val prevSize = alloc<ULongVar>()
        val error = wasmtime_memory_grow(store, memory, delta, prevSize.ptr)
        if (error != null) throw WasmtimeError(error)
        return prevSize.value
    }

    override fun close() {
        type.close()
        nativeHeap.free(memory)
    }
}
