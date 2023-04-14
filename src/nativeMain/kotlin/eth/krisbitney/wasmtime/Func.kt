package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t
import eth.krisbitney.wasmtime.util.toList
import eth.krisbitney.wasmtime.wasm.FuncType
import platform.posix.memcpy
import wasmtime.*

typealias FuncCallback = (caller: Caller, args: List<Val>) -> Result<List<Val>>

/** Func is owned by the Store, and does not need to be deleted by the user */
@OptIn(ExperimentalStdlibApi::class)
class Func(
    store: CPointer<wasmtime_context_t>,
    val func: CPointer<wasmtime_func_t>
) : Extern(store, Extern.Kind.FUNC), AutoCloseable {

    constructor(
        store: Store<*>,
        type: FuncType,
        callback: FuncCallback,
    ) : this(
        store.context.context,
        nativeHeap.alloc<wasmtime_func_t>().apply {
            val cFuncType = FuncType.allocateCValue(type)
            val stableRef = StableRef.create(callback)
            val cCallback: wasmtime_func_callback_t = staticCFunction(::cFuncCallback)
            val envFinalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>> = staticCFunction { ptr: COpaquePointer? ->
                ptr?.asStableRef<FuncCallback>()?.dispose()
            }
            wasmtime_func_new(
                store.context.context,
                cFuncType,
                cCallback,
                stableRef.asCPointer(),
                envFinalizer,
                this.ptr
            )
            FuncType.deleteCValue(cFuncType)
            stableRef.dispose()
        }.ptr
    )

    fun type(): FuncType {
        val ptr = wasmtime_func_type(store, func) ?: throw Error("failed to get function type")
        return FuncType(ptr)
    }

    fun call(args: List<Val>? = null): List<Val> = memScoped {
        val funcType = type()
        val paramTypes = funcType.params
        val resultTypes = funcType.results

        val argsSize = args?.size ?: 0
        require((argsSize) == paramTypes.size) {
            "expected ${paramTypes.size} arguments, got $argsSize"
        }

        val cParams: CArrayPointer<wasmtime_val_t>? = args?.run {
            this.map { Val.allocateCValue(it) }.toCValues().ptr.reinterpret()
        }
        val cResults: CArrayPointer<wasmtime_val_t> = allocArray(resultTypes.size)

        val trap = alloc<CPointerVar<wasm_trap_t>>()
        val error = wasmtime_func_call(
            store,
            func,
            cParams,
            paramTypes.size.convert(),
            cResults,
            resultTypes.size.convert(),
            trap.ptr
        )
        cParams?.let { nativeHeap.free(cParams) }

        if (error != null) {
            throw WasmtimeException(error)
        }

        val trapValue = trap.value
        if (trapValue != null) {
            throw Trap(trapValue)
        }

        return cResults.toList(resultTypes.size)
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

private fun cFuncCallback(
    cEnv: COpaquePointer?,
    cCaller: CPointer<wasmtime_caller_t>?,
    cArgs: CPointer<wasmtime_val_t>?,
    nargs: size_t,
    cResults: CPointer<wasmtime_val_t>?,
    nresults: size_t
): CPointer<wasm_trap_t>? {
    val fn = cEnv!!.asStableRef<FuncCallback>().get()
    val caller = Caller(cCaller!!)
    val args = cArgs!!.toList(nargs.toInt())

    val callbackResult: Result<List<Val>> = fn(caller, args)
    if (callbackResult.isSuccess) {
        val results = callbackResult.getOrThrow()
        var ptr: CPointer<wasmtime_val_t> = cResults!!
        val wasmtimeValSize = sizeOf<wasmtime_val_t>()
        for (i in 0 until nresults.toInt()) {
            val cVal = Val.allocateCValue(results[i])
            memcpy(cResults, cVal, wasmtimeValSize.convert())
            Val.deleteCValue(cVal)
            ptr = interpretCPointer(ptr.rawValue + wasmtimeValSize)
                ?: throw Exception("failed to offset c pointer")
        }
        return null
    } else {
        val error = callbackResult.exceptionOrNull()!!
        val message = error.message ?: "unknown error during Wasm function call"
        return wasmtime_trap_new(message, message.length.convert())
    }
}

