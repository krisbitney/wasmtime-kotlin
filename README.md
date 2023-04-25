# Wasmtime-kt

This project aims to bring Wasmtime support to Kotlin and the JVM. The Kotlin/Native package wraps the Wasmtime C API and adds user experience improvements. It is ready for use!

The Kotlin/JVM package, which is still in its early stages, will wrap the Kotlin/Native package. It is not yet available for use.

Although the code is thoroughly documented following the KDoc standard, the documentation website is not yet published.

## Adding as a dependency

TODO

## Quick Start

TODO

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