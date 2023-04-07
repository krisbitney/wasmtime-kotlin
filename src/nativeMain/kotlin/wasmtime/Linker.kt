package wasmtime

import kotlinx.cinterop.*
import wasm.WasmFuncType

@OptIn(ExperimentalStdlibApi::class)
class Linker(val linker:  CPointer<wasmtime_linker_t>) : AutoCloseable {

    constructor(engine: Engine) :
            this(wasmtime_linker_new(engine.engine) ?: throw Exception("failed to create linker"))

    fun allowShadowing(allowShadowing: Boolean) {
        wasmtime_linker_allow_shadowing(linker, allowShadowing)
    }

    fun define(
        store: Store<*>,
        module: String,
        name: String,
        item: Extern
    ) {
        val error = wasmtime_linker_define(
            linker,
            store.context.context,
            module,
            module.length.convert(),
            name,
            name.length.convert(),
            item.extern
        )
        if (error != null) throw WasmtimeError(error)
    }

    fun <T : Any>defineFunc(
        module: String,
        name: String,
        funcType: WasmFuncType,
        callback: wasmtime_func_callback_t? = null,
        data: T? = null,
        finalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>>? = null
    ) {
        val dataPtr: COpaquePointer? = data?.let { StableRef.create(data).asCPointer() }
        val error = wasmtime_linker_define_func(
            linker,
            module,
            module.length.convert(),
            name,
            name.length.convert(),
            funcType.funcType,
            callback,
            dataPtr,
            finalizer
        )
        if (error != null) {
            dataPtr?.asStableRef<Any>()?.dispose()
            throw WasmtimeError(error)
        }
    }

    fun <T: Any>defineFuncUnchecked(
        module: String,
        name: String,
        funcType: WasmFuncType,
        callback: wasmtime_func_unchecked_callback_t? = null,
        data: T? = null,
        finalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>>? = null
    ) {
        val dataPtr: COpaquePointer? = data?.let { StableRef.create(data).asCPointer() }
        val error = wasmtime_linker_define_func_unchecked(
            linker,
            module,
            module.length.convert(),
            name,
            name.length.convert(),
            funcType.funcType,
            callback,
            dataPtr,
            finalizer
        )
        if (error != null) {
            dataPtr?.asStableRef<Any>()?.dispose()
            throw WasmtimeError(error)
        }
    }

    fun defineWasi() {
        val error = wasmtime_linker_define_wasi(linker)
        if (error != null) throw WasmtimeError(error)
    }

    fun defineInstance(store: Store<*>, name: String, instance: Instance) {
        val error = wasmtime_linker_define_instance(
            linker,
            store.context.context,
            name,
            name.length.convert(),
            instance.instance.ptr
        )
        if (error != null) throw WasmtimeError(error)
    }

    fun instantiate(store: Store<*>, module: Module): Instance {
        val instance = cValue<wasmtime_instance_t>()
        val trap = nativeHeap.alloc<CPointerVar<wasm_trap_t>>()
        val error = wasmtime_linker_instantiate(
            linker,
            store.context.context,
            module.module,
            instance,
            trap.ptr
        )
        if (error != null) {
            nativeHeap.free(trap)
            throw WasmtimeError(error)
        }
        if (trap.value != null) {
            nativeHeap.free(trap)
            throw Trap(trap.value!!)
        }
        nativeHeap.free(trap)
        return Instance(store.context.context, instance.useContents { this })
    }

    fun module(store: Store<*>, name: String, module: Module) {
        val error = wasmtime_linker_module(
            linker,
            store.context.context,
            name,
            name.length.convert(),
            module.module
        )
        if (error != null) throw WasmtimeError(error)
    }

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
            throw WasmtimeError(error)
        }
        return Func(store.context.context, func.ptr)
    }

    fun get(
        store: Store<*>,
        module: String,
        name: String
    ): Extern? {
        val item = nativeHeap.alloc<wasmtime_extern_t>()
        val found = wasmtime_linker_get(
            linker,
            store.context.context,
            module,
            module.length.convert(),
            name,
            name.length.convert(),
            item.ptr
        )
        if (!found) {
            nativeHeap.free(item)
            return null
        }
        return Extern(store.context.context, item.ptr)
    }

    override fun close() {
        wasmtime_linker_delete(linker)
    }
}
