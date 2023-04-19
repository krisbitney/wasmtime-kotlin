package eth.krisbitney.wasmtime

import kotlin.test.*
import eth.krisbitney.wasmtime.wasm.*

class MemoryTest {

    private val engine = Engine()

    @Test
    fun testMemoryType() {
        val store = Store<Unit>(engine)
        val memoryType = MemoryType(Limits(1u, 10u))
        val memory = Memory(store, memoryType)

        assertEquals(memoryType, memory.type)

        memory.close()
        store.close()
    }

    @Test
    fun testMemoryDataSize() {
        val store = Store<Unit>(engine)
        val memoryType = MemoryType(Limits(1u, 10u))
        val memory = Memory(store, memoryType)

        assertEquals(65536, memory.dataSize.toInt())
        assertTrue(memory.data.all { it == 0.toByte() })

        memory.close()
        store.close()
    }

    @Test
    fun testMemorySize() {
        val store = Store<Unit>(engine)
        val memoryType = MemoryType(Limits(1u, 10u))
        val memory = Memory(store, memoryType)

        assertEquals(1u, memory.size)

        memory.close()
        store.close()
    }

    @Test
    fun testMemoryGrow() {
        val store = Store<Unit>(engine)
        val memoryType = MemoryType(Limits(1u, 10u))
        val memory = Memory(store, memoryType)

        val prevSize = memory.grow(2u)
        assertEquals(1u, prevSize)
        assertEquals(3u, memory.size)

        memory.close()
        store.close()
    }

    @Test
    fun testMemoryGrowFail() {
        val store = Store<Unit>(engine)
        val memoryType = MemoryType(Limits(1u, 2u))
        val memory = Memory(store, memoryType)

        assertFailsWith<WasmtimeException> {
            memory.grow(2u)
        }

        memory.close()
        store.close()
    }
}
