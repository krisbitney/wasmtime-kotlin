package eth.krisbitney.wasmtime.wasm

import wasmtime.*
import kotlin.test.*

class ExternTypeTest {

    private val simpleFuncType = FuncType(arrayOf(ValType.I32()), arrayOf(ValType.I32()))
    private val simpleGlobalType = GlobalType(ValType.Kind.I32, Mutability.CONST)
    private val simpleMemoryType = MemoryType(Limits(1u, 10u))
    private val simpleTableType = TableType(ValType.Kind.FUNCREF, Limits(1u, 10u))

    @Test
    fun testFromCValueFuncType() {
        val cFuncType = FuncType.allocateCValue(simpleFuncType)
        assertNotNull(cFuncType)
        assertNotEquals(cFuncType.rawValue.toLong(), 0)
        val cExternType = wasm_functype_as_externtype(cFuncType)!!
        val externType = ExternType.fromCValue(cExternType)
        assertTrue(externType is FuncType)
        assertEquals(ExternType.Kind.FUNC, externType.kind)
    }

    @Test
    fun testFromCValueGlobalType() {
        val cGlobalType = GlobalType.allocateCValue(simpleGlobalType)
        assertNotNull(cGlobalType)
        assertNotEquals(cGlobalType.rawValue.toLong(), 0)
        val cExternType = wasm_globaltype_as_externtype(cGlobalType)!!
        val externType = ExternType.fromCValue(cExternType)
        assertTrue(externType is GlobalType)
        assertEquals(ExternType.Kind.GLOBAL, externType.kind)
    }

    @Test
    fun testFromCValueTableType() {
       val cTableType = TableType.allocateCValue(simpleTableType)
        assertNotNull(cTableType)
        assertNotEquals(cTableType.rawValue.toLong(), 0)
        val cExternType = wasm_tabletype_as_externtype(cTableType)!!
        val externType = ExternType.fromCValue(cExternType)
        assertTrue(externType is TableType)
        assertEquals(ExternType.Kind.TABLE, externType.kind)
    }

    @Test
    fun testFromCValueMemoryType() {
        val cMemoryType = MemoryType.allocateCValue(simpleMemoryType)
        assertNotNull(cMemoryType)
        assertNotEquals(cMemoryType.rawValue.toLong(), 0)
        val cExternType = wasm_memorytype_as_externtype(cMemoryType)!!
        val externType = ExternType.fromCValue(cExternType)
        assertTrue(externType is MemoryType)
        assertEquals(ExternType.Kind.MEMORY, externType.kind)
    }

    @Test
    fun testKindFromValue() {
        assertEquals(ExternType.Kind.FUNC, ExternType.Kind.fromValue(wasm_externkind_enum.WASM_EXTERN_FUNC.value.toUByte()))
        assertEquals(ExternType.Kind.GLOBAL, ExternType.Kind.fromValue(wasm_externkind_enum.WASM_EXTERN_GLOBAL.value.toUByte()))
        assertEquals(ExternType.Kind.TABLE, ExternType.Kind.fromValue(wasm_externkind_enum.WASM_EXTERN_TABLE.value.toUByte()))
        assertEquals(ExternType.Kind.MEMORY, ExternType.Kind.fromValue(wasm_externkind_enum.WASM_EXTERN_MEMORY.value.toUByte()))
    }

    @Test
    fun testInvalidKindFromValue() {
        assertFailsWith<IllegalArgumentException> {
            ExternType.Kind.fromValue(42.toUByte())
        }
    }
}
