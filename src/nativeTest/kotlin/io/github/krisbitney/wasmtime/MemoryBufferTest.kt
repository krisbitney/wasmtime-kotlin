package io.github.krisbitney.wasmtime

import kotlin.test.*

class MemoryBufferTests {

    private lateinit var engine: Engine
    private lateinit var store: Store<Unit>
    private lateinit var memory: Memory

    @BeforeTest
    fun beforeEach() {
        engine = Engine()
        store = Store<Unit>(engine)
        memory = Memory(store, 1u)
    }

    @AfterTest
    fun afterEach() {
        store.close()
        engine.close()
    }

    @Test
    fun testBufferSize() {
        val buffer = memory.buffer
        val expectedSize = 64 * 1024
        assertEquals(expectedSize, buffer.size, "Buffer size should be equal to 64 KiB")
    }

    @Test
    fun testBufferSetAndGet() {
        val buffer = memory.buffer
        val index = 42

        val old = buffer[index]
        buffer[index] = 13
        val value = buffer[index]
        assertEquals(13, value, "Buffer value at index $index should be equal to 13")
        assertNotEquals(old, value, "Old value and new value at index $index should not be equal")
    }

    @Test
    fun testBufferGetThrowsIndexOutOfBoundsException() {
        val buffer = memory.buffer
        assertFailsWith<IndexOutOfBoundsException> { buffer[-1] }
        assertFailsWith<IndexOutOfBoundsException> { buffer[buffer.size] }
    }

    @Test
    fun testBufferSetThrowsIndexOutOfBoundsException() {
        val buffer = memory.buffer
        assertFailsWith<IndexOutOfBoundsException> { buffer[-1] = 0 }
        assertFailsWith<IndexOutOfBoundsException> { buffer[buffer.size] = 0 }
    }

    @Test
    fun testToByteArray() {
        val buffer = memory.buffer
        val byteArray = buffer.toByteArray()

        assertEquals(buffer.size, byteArray.size, "ByteArray size should be equal to the buffer size")
        for (i in byteArray.indices) {
            assertEquals(buffer[i], byteArray[i], "ByteArray element at index $i should be equal to the buffer element at index $i")
        }
    }

    @Test
    fun testCopyInto() {
        val buffer = memory.buffer
        val destination = ByteArray(buffer.size)
        buffer.copyInto(destination)

        for (i in destination.indices) {
            assertEquals(buffer[i], destination[i], "Destination ByteArray element at index $i should be equal to the buffer element at index $i")
        }
    }

    @Test
    fun testCopyIntoWithParameters() {
        val buffer = memory.buffer
        val destination = ByteArray(buffer.size + 10)
        val destinationOffset = 5
        val startIndex = 10
        val endIndex = buffer.size - 10

        buffer.copyInto(destination, destinationOffset, startIndex, endIndex)

        for (i in startIndex until endIndex) {
            assertEquals(buffer[i], destination[destinationOffset + i - startIndex], "Destination ByteArray element at index ${destinationOffset + i - startIndex} should be equal to the buffer element at index $i")
        }
    }

    @Test
    fun testCopyIntoThrowsIllegalArgumentException() {
        val buffer = memory.buffer
        val destination = ByteArray(buffer.size)

        assertFailsWith<IllegalArgumentException> {
            buffer.copyInto(destination, destinationOffset = -1)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.copyInto(destination, startIndex = -1)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.copyInto(destination, endIndex = buffer.size + 1)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.copyInto(destination, startIndex = 10, endIndex = 5)
        }
    }

    @Test
    fun testCopyFrom() {
        val buffer = memory.buffer
        val src = ByteArray(buffer.size) { it.toByte() }

        buffer.copyFrom(src)

        for (i in src.indices) {
            assertEquals(src[i], buffer[i], "Buffer element at index $i should be equal to the source ByteArray element at index $i")
        }
    }

    @Test
    fun testCopyFromWithParameters() {
        val buffer = memory.buffer
        val src = ByteArray(buffer.size) { it.toByte() }
        val startIndex = 10
        val endIndex = src.size - 10
        val destinationOffset = 5

        buffer.copyFrom(src, startIndex, endIndex, destinationOffset)

        for (i in startIndex until endIndex) {
            assertEquals(src[i], buffer[destinationOffset + i - startIndex], "Buffer element at index ${destinationOffset + i - startIndex} should be equal to the source ByteArray element at index $i")
        }
    }

    @Test
    fun testCopyFromThrowsIllegalArgumentException() {
        val buffer = memory.buffer
        val src = ByteArray(buffer.size)

        assertFailsWith<IllegalArgumentException> {
            buffer.copyFrom(src, startIndex = -1)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.copyFrom(src, endIndex = src.size + 1)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.copyFrom(src, startIndex = 10, endIndex = 5)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.copyFrom(src, destinationOffset = -1)
        }
    }
}

