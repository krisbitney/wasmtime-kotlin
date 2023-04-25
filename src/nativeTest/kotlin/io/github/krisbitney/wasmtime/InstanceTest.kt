package io.github.krisbitney.wasmtime

import kotlin.test.*
import io.github.krisbitney.wasmtime.wasm.*
import util.moduleWithSimpleExport
import util.moduleWithSimpleImportAndExport
import util.moduleWithTwoExports

class InstanceTest {

    private val engine = Engine()

    @Test
    fun testConstructor() {
        val store = Store<Unit>(engine)
        val module = Module(engine, moduleWithSimpleImportAndExport)

        val funcType = FuncType(emptyArray(), arrayOf(ValType.I32()))
        val func = Func(store, funcType) { _, _ -> Result.success(listOf(Val(42))) }

        val instance = Instance(store, module, listOf(func))

        val export = instance.getExport("simple_function")
        assertNotNull(export, "Expected export 'simple_function' to be found")
        assertEquals(Extern.Kind.FUNC, export.kind, "Expected export 'simple_function' to be of kind FUNC")
    }

    @Test
    fun testGetExportByName() {
        val store = Store<Unit>(engine)
        val module = Module(engine, moduleWithSimpleExport)
        val instance = Instance(store, module, emptyList())

        val export = instance.getExport("simple_function")
        assertNotNull(export, "Expected export 'simple_function' to be found")
        assertEquals(Extern.Kind.FUNC, export.kind, "Expected export 'simple_function' to be of kind FUNC")

        val undefinedExport = instance.getExport("undefined")
        assertNull(undefinedExport, "Expected export 'undefined' to be null")
    }

    @Test
    fun testGetExportByIndex() {
        val store = Store<Unit>(engine)
        val module = Module(engine, moduleWithTwoExports)
        val instance = Instance(store, module, emptyList())

        val export0 = instance.getExport(0)
        assertNotNull(export0, "Expected export at index 0 to be found")
        assertEquals("first", export0.first, "Expected export at index 0 to have the name 'first'")
        assertEquals(Extern.Kind.FUNC, export0.second.kind, "Expected export at index 0 to be of kind FUNC")

        val export1 = instance.getExport(1)
        assertNotNull(export1, "Expected export at index 1 to be found")
        assertEquals("second", export1.first, "Expected export at index 1 to have the name 'second'")
        assertEquals(Extern.Kind.FUNC, export1.second.kind, "Expected export at index 1 to be of kind FUNC")

        val undefinedExport = instance.getExport(2)
        assertNull(undefinedExport, "Expected export at index 2 to be null")
    }
}