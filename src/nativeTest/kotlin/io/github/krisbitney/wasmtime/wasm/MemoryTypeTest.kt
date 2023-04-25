package io.github.krisbitney.wasmtime.wasm

import kotlinx.cinterop.pointed
import wasmtime.wasm_memorytype_limits
import kotlin.test.*

class MemoryTypeTest {

    private val simpleMemoryType = MemoryType(Limits(1u, 10u))

    @Test
    fun testAllocateCValue() {
        val cMemoryType = MemoryType.allocateCValue(simpleMemoryType)
        assertNotNull(cMemoryType)
        assertNotEquals(cMemoryType.rawValue.toLong(), 0)

        val cLimits = wasm_memorytype_limits(cMemoryType)
        assertNotNull(cLimits)
        assertNotEquals(cLimits.rawValue.toLong(), 0)
        assertEquals(simpleMemoryType.limits.min, cLimits.pointed.min)
        assertEquals(simpleMemoryType.limits.max, cLimits.pointed.max)

        val memoryType = MemoryType(cMemoryType)
        assertEquals(simpleMemoryType.limits.min, memoryType.limits.min)
        assertEquals(simpleMemoryType.limits.max, memoryType.limits.max)
    }

    @Test
    fun testDeleteCValue() {
        val cMemoryType = MemoryType.allocateCValue(simpleMemoryType)
        assertNotNull(cMemoryType)
        assertNotEquals(cMemoryType.rawValue.toLong(), 0)
        MemoryType.deleteCValue(cMemoryType)
    }
}
