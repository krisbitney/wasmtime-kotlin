package io.github.krisbitney.wasmtime.wasm

import kotlin.test.*

class FuncTypeTest {

    private val simpleFuncType = FuncType(arrayOf(ValType.I32()), arrayOf(ValType.I32()))

    @Test
    fun testAllocateCValue() {
        val cFuncType = FuncType.allocateCValue(simpleFuncType)
        assertNotNull(cFuncType)
        assertNotEquals(cFuncType.rawValue.toLong(), 0)

        val newFuncType = FuncType(cFuncType)
        assertEquals(1, newFuncType.params.size)
        assertEquals(ValType.Kind.I32, newFuncType.params[0].kind)
        assertEquals(1, newFuncType.results.size)
        assertEquals(ValType.Kind.I32, newFuncType.results[0].kind)
    }

    @Test
    fun testDeleteCValue() {
        val cFuncType = FuncType.allocateCValue(simpleFuncType)
        assertNotNull(cFuncType)
        assertNotEquals(cFuncType.rawValue.toLong(), 0)
        FuncType.deleteCValue(cFuncType)
    }
}
