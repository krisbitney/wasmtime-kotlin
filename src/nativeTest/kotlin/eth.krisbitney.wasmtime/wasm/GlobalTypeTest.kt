package eth.krisbitney.wasmtime.wasm

import kotlin.test.*

class GlobalTypeTest {

    private val simpleGlobalType = GlobalType(ValType.Kind.I32, Mutability.CONST)

    @Test
    fun testAllocateCValue() {
        val cGlobalType = GlobalType.allocateCValue(simpleGlobalType)
        assertNotNull(cGlobalType)
        assertNotEquals(cGlobalType.rawValue.toLong(), 0)
        val newGlobalType = GlobalType(cGlobalType)
        assertEquals(ValType.Kind.I32, newGlobalType.content)
        assertEquals(Mutability.CONST, newGlobalType.mutability)
    }

    @Test
    fun testDeleteCValue() {
        val cGlobalType = GlobalType.allocateCValue(simpleGlobalType)
        assertNotNull(cGlobalType)
        assertNotEquals(cGlobalType.rawValue.toLong(), 0)
        GlobalType.deleteCValue(cGlobalType)
    }
}
