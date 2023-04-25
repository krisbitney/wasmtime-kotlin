package io.github.krisbitney.wasmtime

import io.github.krisbitney.wasmtime.wasm.MemoryType
import util.invalidWat
import util.moduleWithMemoryImport
import util.moduleWithSimpleExport
import util.moduleWithSimpleImport
import util.moduleWithSimpleImportAndExport
import util.wrapWasmBytes
import kotlin.test.*

class ModuleTest {
    private lateinit var engine: Engine

    @BeforeTest
    fun beforeEach() {
        engine = Engine()
    }

    @AfterTest
    fun afterEach() {
        engine.close()
    }

    @Test
    fun testModuleCreationFromBinary() {
        val module = Module(engine, wrapWasmBytes)
        assertNotNull(module)
        assertNotNull(module.module)
        assertNotEquals(module.module.rawValue.toLong(), 0)
        module.close()
    }

    @Test
    fun testWat2WasmConversion() {
        val result = Module.wat2Wasm(moduleWithSimpleExport)
        assertNull(result.exceptionOrNull())
        val wasm = result.getOrThrow()
        assertNotNull(wasm)
        assertNotEquals(wasm.size, 0)
    }

    @Test
    fun testModuleCreationFromWat() {
        val module = Module(engine, moduleWithSimpleExport)
        assertNotNull(module)
        assertNotNull(module.module)
        assertNotEquals(module.module.rawValue.toLong(), 0)
        module.close()
    }

    @Test
    fun testInvalidWat() {
        val wasm = Module.wat2Wasm(invalidWat).getOrThrow()
        val result = Module.validate(engine, wasm)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun testModuleValidation() {
        val validationResult = Module.validate(engine, wrapWasmBytes)
        assertTrue(validationResult.isSuccess)
    }

    @Test
    fun testInvalidWasmValidation() {
        val invalidWasm = byteArrayOf(0, 97, 115, 109, 1, 0, 0, 0, 3)
        val result = Module.validate(engine, invalidWasm)
        assertNotNull(result.exceptionOrNull())
    }

    @Test
    fun testModuleImports() {
        val module = Module(engine, moduleWithSimpleImport)
        val imports = module.imports()
        assertEquals(1, imports.size)
        assertEquals("env", imports[0].module)
        assertEquals("simple_function", imports[0].name)
        module.close()
    }

    @Test
    fun testModuleExports() {
        val module = Module(engine, moduleWithSimpleExport)
        val exports = module.exports()
        assertEquals(1, exports.size)
        assertEquals("simple_function", exports[0].name)
        module.close()
    }

    @Test
    fun testModuleWithMemoryImport() {
        val module = Module(engine, moduleWithMemoryImport)
        val imports = module.imports()
        assertEquals(1, imports.size)
        assertEquals("env", imports[0].module)
        assertEquals("memory", imports[0].name)
        assertTrue(imports[0].type is MemoryType)
        module.close()
    }

    @Test
    fun testModuleWithSimpleImportAndExport() {
        val module = Module(engine, moduleWithSimpleImportAndExport)
        val imports = module.imports()
        assertEquals(1, imports.size)
        assertEquals("env", imports[0].module)
        assertEquals("simple_function", imports[0].name)

        val exports = module.exports()
        assertEquals(1, exports.size)
        assertEquals("simple_function", exports[0].name)
        module.close()
    }

    @Test
    fun testPolywrapModule() {
        val module = Module(engine, wrapWasmBytes)

        val imports = module.imports()
        assertEquals(10, imports.size)
        assertNotNull(imports.find { it.module == "env" && it.name == "memory" })
        assertNotNull(imports.find { it.module == "wrap" && it.name == "__wrap_abort" })

        val exports = module.exports()
        assertEquals(8, exports.size)
        assertNotNull(exports.find { it.name == "_wrap_invoke" })

        module.close()
    }

    @Test
    fun testModuleSerialization() {
        val module = Module(engine, moduleWithSimpleExport)
        val serialized = module.serialize()
        assertTrue(serialized.isNotEmpty())
        module.close()

        val deserializedModule = Module.deserialize(engine, serialized)
        assertNotNull(deserializedModule)
        assertNotNull(deserializedModule.module)
        assertNotEquals(deserializedModule.module.rawValue.toLong(), 0)
        deserializedModule.close()
    }

    @Test
    fun testModuleDeserialization() {
        val module = Module(engine, moduleWithSimpleExport)
        val serialized = module.serialize()
        module.close()

        val deserializedModule = Module.deserialize(engine, serialized)
        assertNotNull(deserializedModule)
        assertNotEquals(deserializedModule.module.rawValue.toLong(), 0)

        val exports = deserializedModule.exports()
        assertEquals(1, exports.size)
        assertEquals("simple_function", exports[0].name)
        deserializedModule.close()
    }
}