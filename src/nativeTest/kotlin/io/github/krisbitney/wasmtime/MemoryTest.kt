package io.github.krisbitney.wasmtime

import kotlin.test.*
import io.github.krisbitney.wasmtime.wasm.*

class MemoryTest {

    private lateinit var engine: Engine
    private lateinit var store: Store<Unit>

    @BeforeTest
    fun beforeEach() {
        engine = Engine()
        store = Store<Unit>(engine)
    }

    @AfterTest
    fun afterEach() {
        store.close()
        engine.close()
    }

    @Test
    fun testMemoryType() {
        val memoryType = MemoryType(Limits(1u, 10u))
        val memory = Memory(store, memoryType)

        assertEquals(memoryType, memory.type)
    }

    @Test
    fun testMemoryDataSize() {
        val memoryType = MemoryType(Limits(1u, 10u))
        val memory = Memory(store, memoryType)

        assertEquals(65536, memory.buffer.size)
        assertTrue(memory.buffer.all { it == 0.toByte() })
    }

    @Test
    fun testMemorySize() {
        val memoryType = MemoryType(Limits(1u, 10u))
        val memory = Memory(store, memoryType)

        assertEquals(1, memory.size)
    }

    @Test
    fun testMemoryGrow() {
        val memoryType = MemoryType(Limits(1u, 10u))
        val memory = Memory(store, memoryType)

        val prevSize = memory.grow(2u)
        assertEquals(1u, prevSize)
        assertEquals(3, memory.size)
    }

    @Test
    fun testMemoryGrowFail() {
        val memoryType = MemoryType(Limits(1u, 2u))
        val memory = Memory(store, memoryType)

        assertFailsWith<WasmtimeException> {
            memory.grow(2u)
        }
    }
}
