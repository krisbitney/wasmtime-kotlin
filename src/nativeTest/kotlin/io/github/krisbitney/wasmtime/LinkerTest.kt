package io.github.krisbitney.wasmtime

import util.additionModule
import io.github.krisbitney.wasmtime.wasm.FuncType
import io.github.krisbitney.wasmtime.wasm.ValType
import kotlin.test.*

class LinkerTest {
    private lateinit var engine: Engine
    private lateinit var store: Store<Unit>
    private val wasmBytes: ByteArray = Module.wat2Wasm(additionModule).getOrThrow()

    @BeforeTest
    fun beforeEach() {
        engine = Engine()
        store = Store<Unit>(engine)
    }

    @AfterTest
    fun afterEach() {
        store.close()
        engine.close()
    }

    @Test
    fun testAllowShadowing() {
        val linker = Linker(engine)
        linker.allowShadowing(true)
        linker.defineFunc("test", "double", FuncType(arrayOf(ValType.I32()), arrayOf(ValType.I32()))) { _, args ->
            Result.success(listOf(Val(args[0].i32 * 2)))
        }
        linker.defineFunc("test", "double", FuncType(arrayOf(ValType.I32()), arrayOf(ValType.I32()))) { _, args ->
            Result.success(listOf(Val(args[0].i32 * 3)))
        }
        linker.close()
    }

    @Test
    fun testDefine() {
        val linker = Linker(engine)
        val externToDefine = Func(store, FuncType(arrayOf(ValType.I32()), arrayOf(ValType.I32()))) { _, args ->
            Result.success(listOf(Val(args[0].i32 * 2)))
        }

        // Define an extern in the linker
        linker.define(store, "test", "double", externToDefine)

        // Retrieve the exported function and test it
        val extern = linker.get(store, "test", "double")
        assertNotNull(extern)
        assertTrue(extern is Func)
        val result = extern.call(listOf(Val(2)))
        assertEquals(4, result[0].i32)
        linker.close()
    }

    @Test
    fun testDefineFunc() {
        val linker = Linker(engine)

        // Define a function in the linker
        linker.defineFunc("test", "double", FuncType(arrayOf(ValType.I32()), arrayOf(ValType.I32()))) { _, args ->
            Result.success(listOf(Val(args[0].i32 * 2)))
        }

        // Retrieve the exported function and test it
        val extern = linker.get(store, "test", "double")
        assertNotNull(extern)
        assertTrue(extern is Func)
        val result = extern.call(listOf(Val(2)))
        assertEquals(4, result[0].i32)

        linker.close()
    }

    @Test
    fun testDefineWasi() {
        val linker = Linker(engine)
        linker.defineWasi()
        store.context.setWasi { setArgv(listOf("Hello World")) }
        linker.close()
    }

    @Test
    fun testDefineInstance() {
        val linker = Linker(engine)
        val module = Module(engine, wasmBytes)
        val instance = Instance(store, module, listOf())

        // define the instance in the linker
        linker.defineInstance(store, "example", instance)

        // Retrieve the exported function and test it
        val extern = linker.get(store, "example", "add")
        assertNotNull(extern)
        assertTrue(extern is Func)
        val result = extern.call(listOf(Val(2), Val(3)))
        assertEquals(5, result[0].i32)

        module.close()
        linker.close()
    }

    @Test
    fun testInstantiate() {
        val linker = Linker(engine)
        val module = Module(engine, wasmBytes)

        // Instantiate the module using the linker
        val instance = linker.instantiate(store, module)

        // Retrieve the exported function and test it
        val extern = instance.getExport("add")
        assertNotNull(extern)
        assertTrue(extern is Func)
        val result = extern.call(listOf(Val(2), Val(3)))
        assertEquals(5, result[0].i32)

        module.close()
        linker.close()
    }

    @Test
    fun testModule() {
        val linker = Linker(engine)
        val module = Module(engine, wasmBytes)

        // Define the module in the linker
        linker.module(store, "example", module)

        // Retrieve the exported function and test it
        val extern = linker.get(store, "example", "add")
        assertNotNull(extern)
        assertTrue(extern is Func)
        val result = extern.call(listOf(Val(2), Val(3)))
        assertEquals(5, result[0].i32)

        module.close()
        linker.close()
    }

    @Test
    fun testGetDefault() {
        val linker = Linker(engine)
        val module = Module(engine, wasmBytes)

        // Create an instance and define it in the linker
        val instance = Instance(store, module, listOf())
        linker.defineInstance(store, "example", instance)

        // Retrieve the default exported function and test it
        val func = linker.getDefault(store, "example")
        assertNotNull(func)
        val result = func.call(listOf(Val(2), Val(3)))
        assertEquals(5, result[0].i32)

        module.close()
        linker.close()
        
    }


    @Test
    fun testGetNotFound() {
        val linker = Linker(engine)
        val extern = linker.get(store, "nonexistent", "nonexistent")
        assertNull(extern)
    }
}
