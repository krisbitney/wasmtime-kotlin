package io.polywrap.wasmtime

import kotlinx.cinterop.*
import wasmtime.*

//typealias FuncUncheckedCallback = CPointer<CFunction<(
//    env: COpaquePointer?,
//    caller: CPointer<wasmtime_caller_t>,
//    argsAndResults: CPointer<wasmtime_val_raw_t>,
//    nArgsAndResults: size_t
//) -> CPointer<wasm_trap_t>?>>

// TODO: make this class usable from Kotlin without using the C API

class FuncUnchecked(
    private val store: CPointer<wasmtime_context_t>,
    val func: CPointer<wasmtime_func_t>,
) {
    constructor(
        store: Store<*>,
        type: CPointer<wasm_functype_t>,
        callback: wasmtime_func_unchecked_callback_t,
        env: COpaquePointer? = null,
        finalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>>? = null
    ) : this(
        store.context.context,
        nativeHeap.alloc<wasmtime_func_t>().apply {
            wasmtime_func_new_unchecked(store.context.context, type, callback, env, finalizer, this.ptr)
        }.ptr
    )

    fun call(argsAndResults: CArrayPointer<wasmtime_val_raw_t>): Unit = memScoped {
        val trap = alloc<CPointerVar<wasm_trap_t>>()
        val error = wasmtime_func_call_unchecked(store, func, argsAndResults, trap.ptr)

        if (error != null) {
            throw WasmtimeException(error)
        }

        val trapValue = trap.value
        if (trapValue != null) {
            throw Trap(trapValue)
        }
    }
}