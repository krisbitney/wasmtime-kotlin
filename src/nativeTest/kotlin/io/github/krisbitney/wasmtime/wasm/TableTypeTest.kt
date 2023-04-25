package io.github.krisbitney.wasmtime.wasm

import kotlinx.cinterop.pointed
import wasmtime.wasm_tabletype_limits
import kotlin.test.*

class TableTypeTest {

    private val simpleTableType = TableType(ValType.Kind.FUNCREF, Limits(1u, 10u))

    @Test
    fun testAllocateCValue() {
        val cTableType = TableType.allocateCValue(simpleTableType)
        assertNotNull(cTableType)
        assertNotEquals(cTableType.rawValue.toLong(), 0)

        val cLimits = wasm_tabletype_limits(cTableType)
        assertNotNull(cLimits)
        assertNotEquals(cLimits.rawValue.toLong(), 0)
        assertEquals(simpleTableType.limits.min, cLimits.pointed.min)
        assertEquals(simpleTableType.limits.max, cLimits.pointed.max)

        val newTableType = TableType(cTableType)
        assertEquals(ValType.Kind.FUNCREF, newTableType.element)
        assertEquals(1u, newTableType.limits.min)
        assertEquals(10u, newTableType.limits.max)
    }

    @Test
    fun testDeleteCValue() {
        val cTableType = TableType.allocateCValue(simpleTableType)
        assertNotNull(cTableType)
        assertNotEquals(cTableType.rawValue.toLong(), 0)
        TableType.deleteCValue(cTableType)
    }
}
