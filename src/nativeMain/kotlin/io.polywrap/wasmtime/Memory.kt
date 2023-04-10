package io.polywrap.wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t
import io.polywrap.wasmtime.wasm.WasmLimits
import io.polywrap.wasmtime.wasm.MemoryType
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class Memory(
    private val store: CPointer<wasmtime_context_t>,
    val memory: CPointer<wasmtime_memory_t>
) : AutoCloseable {

    constructor(store: Store<*>, min: UInt = 0u, max: UInt = WasmLimits.LIMITS_MAX_DEFAULT) :
            this(
                store.context.context,
                nativeHeap.alloc<wasmtime_memory_t>().apply {
                    val memoryType = MemoryType(min, max)
                    val error = wasmtime_memory_new(store.context.context, memoryType.memoryType, this.ptr)
                    memoryType.close()
                    if (error != null) {
                        memoryType.close()
                        nativeHeap.free(this)
                        throw WasmtimeException(error)
                    }
                }.ptr
            )

    val type: MemoryType
        get() {
        val memoryType = wasmtime_memory_type(store, memory) ?: throw Error("failed to get memory type")
        return MemoryType(memoryType)
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
        if (error != null) throw WasmtimeException(error)
        return prevSize.value
    }

    override fun close() {
        nativeHeap.free(memory)
    }
}