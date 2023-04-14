package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.wasm.FuncType
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class Linker(val linker:  CPointer<wasmtime_linker_t>) : AutoCloseable {

    constructor(engine: Engine) :
            this(wasmtime_linker_new(engine.engine) ?: throw Exception("failed to create linker"))

    fun allowShadowing(allowShadowing: Boolean): Linker = this.apply {
        wasmtime_linker_allow_shadowing(linker, allowShadowing)
    }

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

    fun <T : Any>defineFunc(
        module: String,
        name: String,
        funcType: FuncType,
        callback: wasmtime_func_callback_t? = null,
        data: T? = null,
        finalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>>? = null
    ): Linker = this.apply {
        val cFuncType = FuncType.allocateCValue(funcType)
        val dataPtr: COpaquePointer? = data?.let { StableRef.create(data).asCPointer() }
        val error = wasmtime_linker_define_func(
            linker,
            module,
            module.length.convert(),
            name,
            name.length.convert(),
            cFuncType,
            callback,
            dataPtr,
            finalizer
        )
        FuncType.deleteCValue(cFuncType)
        dataPtr?.asStableRef<Any>()?.dispose()
        if (error != null) {
            throw WasmtimeException(error)
        }
    }

//    fun <T: Any>defineFuncUnchecked(
//        module: String,
//        name: String,
//        funcType: FuncType,
//        callback: wasmtime_func_unchecked_callback_t? = null,
//        data: T? = null,
//        finalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>>? = null
//    ): Linker = this.apply {
//        val cFuncType = FuncType.allocateCValue(funcType)
//        val dataPtr: COpaquePointer? = data?.let { StableRef.create(data).asCPointer() }
//        val error = wasmtime_linker_define_func_unchecked(
//            linker,
//            module,
//            module.length.convert(),
//            name,
//            name.length.convert(),
//            cFuncType,
//            callback,
//            dataPtr,
//            finalizer
//        )
//        FuncType.deleteCValue(cFuncType)
//        dataPtr?.asStableRef<Any>()?.dispose()
//        if (error != null) {
//            throw WasmtimeException(error)
//        }
//    }

    fun defineWasi(): Linker = this.apply {
        val error = wasmtime_linker_define_wasi(linker)
        if (error != null) throw WasmtimeException(error)
    }

    fun defineInstance(store: Store<*>, name: String, instance: Instance): Linker = this.apply {
        val error = wasmtime_linker_define_instance(
            linker,
            store.context.context,
            name,
            name.length.convert(),
            instance.instance.ptr
        )
        if (error != null) throw WasmtimeException(error)
    }

    fun instantiate(store: Store<*>, module: Module): Instance = memScoped {
        val instance = cValue<wasmtime_instance_t>()
        val trap = alloc<CPointerVar<wasm_trap_t>>()
        val error = wasmtime_linker_instantiate(
            linker,
            store.context.context,
            module.module,
            instance,
            trap.ptr
        )
        if (error != null) {
            throw WasmtimeException(error)
        }
        if (trap.value != null) {
            throw Trap(trap.value!!)
        }
        return Instance(store.context.context, instance.useContents { this })
    }

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
        return Func(store.context.context, func.ptr)
    }

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
            Extern.fromCValue(store.context.context, item.ptr)
        } else {
            null
        }
    }

    override fun close() {
        wasmtime_linker_delete(linker)
    }
}
