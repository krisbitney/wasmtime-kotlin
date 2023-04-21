package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.wasm.FuncType
import wasmtime.*

/**
 * Used to conveniently link together and instantiate Wasm
 * modules. This type is intended to make it easier to manage a set of modules
 * that link together, or to make it easier to link WebAssembly modules to WASI.
 *
 * A [Linker] is a higher level way to instantiate a module than
 * the [Instance] constructor since it works at the "string" level
 * of imports rather than requiring 1:1 mappings.
 *
 * @property linker The native Wasmtime linker pointer.
 * @constructor Creates a new linker for the specified [Engine].
 * @throws Exception If failed to create a new linker.
 */
@OptIn(ExperimentalStdlibApi::class)
class Linker(val linker:  CPointer<wasmtime_linker_t>) : AutoCloseable {

    constructor(engine: Engine) :
            this(wasmtime_linker_new(engine.engine) ?: throw Exception("failed to create linker"))

    /**
     * Configures whether this linker allows later definitions to shadow
     * previous definitions. By default, this setting is `false`.
     *
     * @param allowShadowing A boolean value indicating whether shadowing is allowed.
     * @return This [Linker] instance.
     */
    fun allowShadowing(allowShadowing: Boolean): Linker = this.apply {
        wasmtime_linker_allow_shadowing(linker, allowShadowing)
    }

    /**
     * Defines a new item in this linker.
     *
     * @param store The store that the `item` is owned by.
     * @param module The module name the item is defined under.
     * @param name The field name the item is defined under.
     * @param item The item that is being defined in this linker.
     * @return This [Linker] instance.
     * @throws [WasmtimeException] If the definition failed.
     */
    fun define(
        store: Store<*>,
        module: String,
        name: String,
        item: Extern
    ): Linker = this.apply {
        val wasmExtern = Extern.allocateCValue(item)
        val error = wasmtime_linker_define(
            linker,
            store.context.context,
            module,
            module.length.convert(),
            name,
            name.length.convert(),
            wasmExtern
        )
        Extern.deleteCValue(wasmExtern)
        if (error != null) throw WasmtimeException(error)
    }

    /**
     * Defines a new function in this linker.
     *
     * @param module The module name the item is defined under.
     * @param name The field name the item is defined under.
     * @param type The type of the function that's being defined.
     * @param callback The host callback to invoke when the function is called (optional).
     * @return This [Linker] instance.
     * @throws [WasmtimeException] If the definition failed.
     */
    fun defineFunc(
        module: String,
        name: String,
        type: FuncType,
        callback: FuncCallback
    ): Linker = this.apply {
        val cFuncType = FuncType.allocateCValue(type)
        val callbackRef = StableRef.create(callback)
        val cCallback: wasmtime_func_callback_t = staticCFunction(::cFuncCallback)
        val envFinalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>> = staticCFunction { ptr: COpaquePointer? ->
            ptr?.asStableRef<FuncCallback>()?.dispose()
        }

        val error = wasmtime_linker_define_func(
            linker,
            module,
            module.length.convert(),
            name,
            name.length.convert(),
            cFuncType,
            cCallback,
            callbackRef.asCPointer(),
            envFinalizer
        )
        FuncType.deleteCValue(cFuncType)
        if (error != null) {
            throw WasmtimeException(error)
        }
    }

    /**
     * Defines the WebAssembly System Interface (WASI) functions in this linker.
     *
     * This method makes WASI functions available in the linker, which can then be used
     * by WebAssembly modules. Note that when creating an instance within a [Store],
     * the [Store]'s [Context] also needs to have its WASI settings configured using the
     * [Context.setWasi] method for WASI functions to work correctly.
     *
     * @throws [WasmtimeException] if there is an error while defining WASI functions.
     */
    fun defineWasi(): Linker = this.apply {
        val error = wasmtime_linker_define_wasi(linker)
        if (error != null) throw WasmtimeException(error)
    }

    /**
     * Defines the provided [instance] under the specified [name] in this linker.
     *
     * This method takes all of the exports of the provided [instance] and defines them
     * under a module called [name] with a field name as the export's own name.
     *
     * @param store the store that owns the [instance].
     * @param name the module name to define the [instance] under.
     * @param instance a previously-created instance.
     * @throws [WasmtimeException] if there is an error while defining the instance.
     */
    fun defineInstance(store: Store<*>, name: String, instance: Instance): Linker = this.apply {
        val error = wasmtime_linker_define_instance(
            linker,
            store.context.context,
            name,
            name.length.convert(),
            instance.instance
        )
        if (error != null) throw WasmtimeException(error)
    }

    /**
     * Instantiates a WebAssembly module with this linker, using
     * definitions within this linker to satisfy imports.
     *
     * @param store The store that the module will be instantiated with.
     * @param module The module to instantiate.
     * @return A new [Instance] representing the instantiated module.
     * @throws [WasmtimeException] If the instantiation failed.
     */
    fun instantiate(store: Store<*>, module: Module): Instance = memScoped {
        val instance = nativeHeap.alloc<wasmtime_instance_t>()
        val trap = alloc<CPointerVar<wasm_trap_t>>()
        val error = wasmtime_linker_instantiate(
            linker,
            store.context.context,
            module.module,
            instance.ptr,
            trap.ptr
        )
        if (error != null) {
            throw WasmtimeException(error)
        }
        if (trap.value != null) {
            throw Trap(trap.value!!)
        }
        return Instance(store, instance.ptr)
    }

    /**
     * Defines automatic instantiations of a [module] in this linker.
     *
     * This method handles automatic instantiation and initialization of [module]
     * according to Commands and Reactors specification.
     *
     * @param store the store that is used to instantiate [module].
     * @param name the name of the module within the linker.
     * @param module the module that's being instantiated.
     * @throws [WasmtimeException] if the module could not be instantiated or added.
     */
    fun module(store: Store<*>, name: String, module: Module): Linker = this.apply {
        val error = wasmtime_linker_module(
            linker,
            store.context.context,
            name,
            name.length.convert(),
            module.module
        )
        if (error != null) throw WasmtimeException(error)
    }

    /**
     * Retrieves the "default export" of the named module in this linker.
     *
     * This method looks up the default export for the module with the provided [name]
     * and returns a corresponding [Func] object.
     *
     * @param store the store to load a function into.
     * @param name the name of the module to get the default export for.
     * @return the extracted default function.
     * @throws [WasmtimeException] if the default export could not be found.
     */
    fun getDefault(store: Store<*>, name: String): Func {
        val func = nativeHeap.alloc<wasmtime_func_t>()
        val error = wasmtime_linker_get_default(
            linker,
            store.context.context,
            name,
            name.length.convert(),
            func.ptr
        )
        if (error != null) {
            nativeHeap.free(func)
            throw WasmtimeException(error)
        }
        store.own(func.ptr)
        return Func(store.context.context, func.ptr)
    }

    /**
     * Retrieves an [Extern] object for the specified [module] and [name] from this linker.
     *
     * This method looks up the export with the given [name] in the module with the provided
     * [module] name and returns a corresponding [Extern] object if found, or `null` otherwise.
     *
     * @param store the store to load an extern into.
     * @param module the name of the module to get the export from.
     * @param name the name of the export to retrieve.
     * @return the [Extern] object if found, `null` otherwise.
     */
    fun get(
        store: Store<*>,
        module: String,
        name: String
    ): Extern? = memScoped {
        val item = alloc<wasmtime_extern_t>()
        val found = wasmtime_linker_get(
            linker,
            store.context.context,
            module,
            module.length.convert(),
            name,
            name.length.convert(),
            item.ptr
        )
        return if (found) {
            val extern = Extern.fromCValue(store.context.context, item.ptr)
            when (extern) {
                is Func -> store.own(extern.func)
                is Table -> store.own(extern.table)
                is Memory -> store.own(extern.memory)
                is Global -> store.own(extern.global)
            }
            extern
        } else {
            null
        }
    }

    override fun close() {
        wasmtime_linker_delete(linker)
    }
}
