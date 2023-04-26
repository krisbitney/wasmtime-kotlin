package io.github.krisbitney.wasmtime.util

import io.github.krisbitney.wasmtime.*
import io.github.krisbitney.wasmtime.wasm.ValType
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
class ReadmeTest {

    @Test
    fun testQuickStart() {
        // a mock state
        data class State(var value: Int)

        // A mock wat with an exported function `add` that sums two parameters
        // The module expects imports `multiply` and `memory`
        val wat = """(module
  (import "env" "memory" (memory 1))
  (import "wrap" "multiply" (func (param ${'$'}x i32) (param ${'$'}y i32) (result i32)))
  (func ${'$'}add (param ${'$'}x i32) (param ${'$'}y i32) (result i32)
    local.get ${'$'}x
    local.get ${'$'}y
    i32.add)
  (export "add" (func ${'$'}add))
)
"""

        // Configure Wasmtime in the Engine constructor
        Engine() {
            setMaxWasmStack(4096u)
        }.use { engine ->
            // A Store manages state and Extern objects
            Store(engine, State(42)).use { store ->
                // A wasm module can be instantiated from a byte array or wat string
                Module(engine, wat).use { module ->
                    // The linker is used to conveniently link together and instantiate Wasm modules
                    Linker(engine).use { linker ->
                        // Manage the Wasm instance's memory
                        val memory = Memory(store, 1u)
                        linker.define(store, "env", "memory", memory)

                        // The FuncFactory provides a convenient UX for defining imports
                        val import: Func  = FuncFactory.wrap(store, ValType.I32(), ValType.I32(), ValType.I32()) {
                            x: Int, y: Int ->
                            val result = x * y
                            store.data!!.value = result
                            result
                        }
                        linker.define(store, "wrap", "multiply", import)

                        // We can use the linker to create instance of the module
                        // The linker automatically selects the correct imports from those we defined
                        val instance: Instance = linker.instantiate(store, module)

                        // Get an export from the instance
                        val addFunc: Func = instance.getExport("add") as? Func ?: throw Exception("no `add` export")

                        // FuncFactory can wrap a Func and return a standard Kotlin lambda with the expected interface
                        val addLambda = FuncFactory.producer(addFunc, ValType.I32(), ValType.I32(), ValType.I32())
                        val sum: Int = addLambda(22, 20)
                        assertEquals(42, sum)

                        // Alternatively, we can call the `add` method directly on the Func
                        val addResult: List<Val> = addFunc.call(listOf(Val(22), Val(20)))
                        assertEquals(1, addResult.size)
                        assertEquals(Val.Kind.I32, addResult[0].kind)
                        assertEquals(42, addResult[0].i32)
                    }
                }
            }
        }
    }
}