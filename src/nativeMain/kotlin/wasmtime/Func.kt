package wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t
import toList
import wasm.WasmFuncType

//typealias FuncCallback = CPointer<CFunction<(
//    env: COpaquePointer?,
//    caller: CPointer<wasmtime_caller_t>,
//    args: CPointer<wasmtime_val_t>,
//    nargs: size_t,
//    results: CPointer<wasmtime_val_t>,
//    nresults: size_t
//) -> CPointer<wasm_trap_t>?>>?

@OptIn(ExperimentalStdlibApi::class)
class Func(
    val store: CPointer<wasmtime_context_t>,
    val func: CPointer<wasmtime_func_t>
) : AutoCloseable {

    constructor(
        store: Store<*>,
        type: WasmFuncType,
        callback: wasmtime_func_callback_t? = null,
        env: COpaquePointer? = null,
        envFinalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>>? = null
    ) : this(
        store.context.context,
        nativeHeap.alloc<wasmtime_func_t>().apply {
            wasmtime_func_new(store.context.context, type.funcType, callback, env, envFinalizer, this.ptr)
        }.ptr
    )

    fun type(): WasmFuncType {
        val ptr = wasmtime_func_type(store, func) ?: throw Error("failed to get function type")
        return WasmFuncType(ptr)
    }

    fun call(
        args: CArrayPointer<wasmtime_val_t>,
        nargs: size_t,
        results: CArrayPointer<wasmtime_val_t>,
        nresults: size_t
    ): Unit = memScoped {
        val trap = alloc<CPointerVar<wasm_trap_t>>()
        val error = wasmtime_func_call(store, func, args, nargs, results, nresults, trap.ptr)

        if (error != null) {
            throw WasmtimeError(error)
        }

        val trapValue = trap.value
        if (trapValue != null) {
            throw Trap(trapValue)
        }
    }

    fun call(args: List<Val>? = null): List<Val> = memScoped {
        val funcType = type()
        val paramTypes = funcType.params()
        val resultTypes = funcType.results()

        val argsSize = args?.size ?: 0
        require((argsSize) == paramTypes.size) {
            "expected ${paramTypes.size} arguments, got $argsSize"
        }

        val params: CArrayPointer<wasmtime_val_t>? = args?.run {
            this.map { it.wasmtimeVal }.toCValues().ptr.reinterpret()
        }
        val results: CArrayPointer<wasmtime_val_t> = nativeHeap.allocArray(resultTypes.size)

        val trap = alloc<CPointerVar<wasm_trap_t>>()
        val error = wasmtime_func_call(
            store,
            func,
            params,
            paramTypes.size.convert(),
            results,
            resultTypes.size.convert(),
            trap.ptr
        )

        if (error != null) {
            throw WasmtimeError(error)
        }

        val trapValue = trap.value
        if (trapValue != null) {
            throw Trap(trapValue)
        }

        return results.toList(resultTypes.size)
    }

    companion object {
        fun fromRaw(raw: size_t, store: Store<*>): Func {
            val ret = nativeHeap.alloc<wasmtime_func_t>()
            wasmtime_func_from_raw(store.context.context, raw, ret.ptr)
            return Func(store.context.context, ret.ptr)
        }
    }

    fun toRaw(): size_t {
        return wasmtime_func_to_raw(store, func)
    }

    override fun close() {
        nativeHeap.free(func)
    }
}

