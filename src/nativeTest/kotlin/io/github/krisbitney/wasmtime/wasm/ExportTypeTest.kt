package io.github.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*
import kotlin.test.*

class ExportTypeTest {

    @Test
    fun testConstructorFromCPointer() = memScoped {
        val name = "test_name"
        val funcType = FuncType(arrayOf(ValType.I32()), arrayOf(ValType.I32()))

        val cName = alloc<wasm_name_t>()
        wasm_name_new_from_string(cName.ptr, name)

        val cFuncType = FuncType.allocateCValue(funcType)
        val cExternType = wasm_functype_as_externtype(cFuncType)!!
        val cExportType = wasm_exporttype_new(cName.ptr, cExternType)
        assertNotNull(cExportType)
        assertNotEquals(cExportType.rawValue.toLong(), 0)

        val exportType = ExportType(cExportType)
        assertEquals(name, exportType.name)
        assertEquals(funcType.kind, exportType.type.kind)
    }
}
