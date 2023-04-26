# Wasmtime-kt

This project aims to bring Wasmtime support to Kotlin and the JVM. The Kotlin/Native package wraps the Wasmtime C API and adds user experience improvements. It is ready for use!

The Kotlin/JVM package, which is still in its early stages, will wrap the Kotlin/Native package. It is not yet available for use.

Although the code is thoroughly documented following the KDoc standard, the documentation website is not yet published.

## Adding as a dependency

```kotlin
val nativeMain by getting {
    dependencies {
        implementation("io.github.krisbitney:wasmtime-kt:1.0.0")
    }
}
```

## Quick Start

```kotlin
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
```

## Memory Management

The Kotlin/Native package can be used without any direct interaction with C. Class instances that implement the `Autocloseable` interface need to be closed when you're done using them in order to free the associated memory resources. This can be done by calling their `close` method, or with the `use` scope. `Extern` and `Instance` instances do not need to be freed; they are owned by the `Store` and will be freed when the `Store` is closed. 

The following class types need to be manually closed:
- `Engine`
- `Store`
- `Module`
- `Linker`
- `ExternRef`

#### Warning
- Never try to close an instance twice
- Never use an instance after it has been closed or freed.

## Configuration

You can configure Wasmtime with the `Engine` constructor, which accepts a `Config` builder function.

```kotlin
// Note: this example does not use all the available configuration options
val engine = Engine {
    setStaticMemoryMaximumSize(1024uL)
    setStaticMemoryGuardSize(4096uL)
    setDynamicMemoryGuardSize(8192uL)
    setStrategy(Config.WasmtimeStrategy.CRANELIFT)
    setCraneliftOptLevel(Config.OptLevel.SPEED_AND_SIZE)
    setMaxWasmStack(4096u)
    setDebugInfo(true)
}
```

You can configure Wasi with the `setWasi` method in the `Context` class, which can be accessed through the `Store`.

```kotlin
val context = store.context
// Note: this example does not use all the available configuration options
val result = context.setWasi {
    setArgv(listOf("test"))
    setEnv(mapOf("TEST" to "test"))
}
assertTrue(result.isSuccess)
```

## Contributing

Contributions are welcome! Please fork the repo and make a pull request to get your contribution reviewed and merged.

#### Build and Test

`./gradlew build`