package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*
import kotlin.test.*

class ImportTypeTest {

    @Test
    fun testConstructorFromCPointer() = memScoped {
        val module = "test_module"
        val name = "test_name"
        val memoryType = MemoryType(Limits(1u, 10u))

        val cModule = alloc<wasm_name_t>()
        wasm_name_new_from_string(cModule.ptr, module)
        val cName = alloc<wasm_name_t>()
        wasm_name_new_from_string(cName.ptr, name)

        val cMemoryType = MemoryType.allocateCValue(memoryType)
        val cExternType = wasm_memorytype_as_externtype(cMemoryType)!!
        val cImportType = wasm_importtype_new(cModule.ptr, cName.ptr, cExternType)
        assertNotNull(cImportType)
        assertNotEquals(cImportType.rawValue.toLong(), 0)

        val importType = ImportType(cImportType)
        assertEquals(module, importType.module)
        assertEquals(name, importType.name)
        assertEquals(memoryType.kind, importType.type.kind)
    }
}
