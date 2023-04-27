package io.github.krisbitney.wasmtime

import io.github.krisbitney.wasmtime.wasm.Limits
import kotlinx.cinterop.*
import io.github.krisbitney.wasmtime.wasm.MemoryType
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
class Memory(
    store: CPointer<wasmtime_context_t>,
    val memory: CPointer<wasmtime_memory_t>
) : Extern(store, Extern.Kind.MEMORY) {

    /**
     * Retrieves the [MemoryType] of this memory object.
     */
    val type: MemoryType by lazy {
        val memoryType = wasmtime_memory_type(this.store, memory) ?: throw Exception("failed to get memory type")
        MemoryType(memoryType)
    }

    /**
     * Retrieves the length of this linear memory in WebAssembly pages of 64 KiB.
     */
    val size: Int get() = wasmtime_memory_size(store, memory).toInt()

    /**
     * Retrieves the memory object's data as a [ByteArray].
     * Changing the content of the ByteArray does not change the underlying memory data
     */
    val buffer: Buffer = Buffer(store, memory)

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
                    store.own(this.ptr)
                }.ptr
            )

    /**
     * Creates a new memory object within the provided [store] and the specified memory limits.
     *
     * @param store A [Store] in which to create the memory object.
     * @param min The minimum number of WebAssembly pages in the memory.
     * @param max The maximum number of WebAssembly pages in the memory,
     * or [Limits.LIMITS_MAX_DEFAULT] if no maximum is specified.
     */
    constructor(store: Store<*>, min: UInt = 0u, max: UInt = Limits.LIMITS_MAX_DEFAULT) :
            this(store, MemoryType(min, max))

    /**
     * Grows the memory by the specified [delta] number of pages.
     * A WebAssembly page is 64 KiB in size.
     *
     * @param delta The number of pages to grow the memory by.
     * @return The previous size of the memory, in WebAssembly pages.
     * @throws [WasmtimeException] If the memory cannot be grown.
     */
    fun grow(delta: UInt): UInt = memScoped {
        val prevSize = alloc<ULongVar>()
        val error = wasmtime_memory_grow(store, memory, delta.toULong(), prevSize.ptr)
        if (error != null) throw WasmtimeException(error)
        return prevSize.value.toUInt()
    }

    /**
     * A custom [ByteArray] implementation that provides a mutable view of the underlying C array of [UByte]s.
     * The [Buffer] class allows changes to the content of the array to be directly reflected in the C array.
     * While the content of [Buffer] is mutable, its size is managed by the underlying C array.
     *
     * [Buffer] extends [AbstractList] and implements [RandomAccess] to provide list behavior and fast random
     * access to its elements.
     *
     * @property size The size of the [Buffer], which is equal to the size of the underlying C array.
     */
    class Buffer(
        private val store: CPointer<wasmtime_context_t>,
        private val memory: CPointer<wasmtime_memory_t>
    ) : AbstractList<Byte>(), RandomAccess {

        private val dataPtr: CPointer<UByteVar>
            get() = wasmtime_memory_data(store, memory) ?: throw Exception("failed to get memory data")

        override val size: Int get() = wasmtime_memory_data_size(store, memory).toInt()

        override operator fun get(index: Int): Byte {
            if (index < 0 || index >= size) {
                throw IndexOutOfBoundsException("Index out of bounds: $index")
            }
            return dataPtr[index].toByte()
        }

        /**
         * Replaces the element at the specified position in this list with the specified element.
         *
         * @return the element previously at the specified position.
         */
        operator fun set(index: Int, element: Byte): Byte {
            if (index < 0 || index >= size) {
                throw IndexOutOfBoundsException("Index out of bounds: $index")
            }
            val previous = dataPtr[index]
            dataPtr[index] = element.toUByte()
            return previous.toByte()
        }

        /**
         * Copies the [Buffer] data into a new [ByteArray] and returns the result.
         */
        fun toByteArray(): ByteArray = dataPtr.readBytes(size)

        /**
         * Copies this buffer or its subrange into the destination [ByteArray] and returns that array.
         *
         * @param destination - the array to copy to.
         * @param destinationOffset - the position in the destination array to copy to, 0 by default.
         * @param startIndex - the beginning (inclusive) of the subrange to copy, 0 by default.
         * @param endIndex - the end (exclusive) of the subrange to copy, size of this buffer by default.
         *
         * @return the destination array.
         *
         * @throws IllegalArgumentException when startIndex or endIndex is out of range of this buffer's indices or when
         * startIndex > endIndex.
         * @throws IllegalArgumentException - when the subrange doesn't fit into the destination array starting at the
         * specified destinationOffset, or when that index is out of the destination array indices range.
         */
        fun copyInto(
            destination: ByteArray,
            destinationOffset: Int = 0,
            startIndex: Int = 0,
            endIndex: Int = size
        ): ByteArray {
            require(startIndex in 0..size) { "startIndex ($startIndex) is out of range: 0..$size" }
            require(endIndex in 0..size) { "endIndex ($endIndex) is out of range: 0..$size" }
            require(startIndex <= endIndex) { "startIndex ($startIndex) is greater than endIndex ($endIndex)" }

            val length = endIndex - startIndex
            require(destinationOffset in 0..destination.size) { "destinationOffset ($destinationOffset) is out of range: 0..${destination.size}" }
            require(destinationOffset + length <= destination.size) { "The subrange doesn't fit into the destination array starting at destinationOffset ($destinationOffset)" }

            for (i in 0 until length) {
                destination[destinationOffset + i] = dataPtr[startIndex + i].toByte()
            }

            return destination
        }

        /**
         * Copies a [ByteArray] or its subrange into this buffer.
         *
         * @param src - the array to copy from.
         * @param startIndex - the beginning (inclusive) of the source array's subrange to copy, 0 by default.
         * @param endIndex - the end (exclusive) of the subrange to copy, size of the source array by default.
         * @param destinationOffset - the position in this buffer to copy to, 0 by default.
         *
         * @throws IllegalArgumentException when startIndex or endIndex is out of range of the source array's indices or when
         * startIndex > endIndex.
         * @throws IllegalArgumentException - when the subrange doesn't fit into this buffer starting at the
         * specified destinationOffset, or when that index is out of this buffer's indices range.
         */
        fun copyFrom(
            src: ByteArray,
            startIndex: Int = 0,
            endIndex: Int = src.size,
            destinationOffset: Int = 0,
        ) {
            require(startIndex in 0..src.size) { "startIndex ($startIndex) is out of range: 0..$src.size" }
            require(endIndex in 0..src.size) { "endIndex ($endIndex) is out of range: 0..$src.size" }
            require(startIndex <= endIndex) { "startIndex ($startIndex) is greater than endIndex ($endIndex)" }

            val length = endIndex - startIndex
            require(destinationOffset in 0..size) { "destinationOffset ($destinationOffset) is out of range: 0..${size}" }
            require(destinationOffset + length <= size) { "The subrange doesn't fit into the destination array starting at destinationOffset ($destinationOffset)" }

            for (i in 0 until length) {
                dataPtr[destinationOffset + i] = src[startIndex + i].toUByte()
            }
        }
    }
}
