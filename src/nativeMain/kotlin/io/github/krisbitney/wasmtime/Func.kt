package io.github.krisbitney.wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t
import io.github.krisbitney.wasmtime.util.toList
import io.github.krisbitney.wasmtime.wasm.FuncType
import platform.posix.memcpy
import wasmtime.*

/**
 * A typealias for function callbacks used in the [Func] class.
 * Represents a higher-level Kotlin function that can be called from WebAssembly code.
 *
 * @param caller A [Caller] object representing the WebAssembly caller.
 * @param args A list of [Val] objects representing the arguments passed to the function.
 * @return A [Result] containing a list of [Val] objects representing the results of the function call.
 */
typealias FuncCallback = (caller: Caller, args: List<Val>) -> Result<List<Val>>

/**
 * Represents a function in the WebAssembly store.
 *
 * @property store The [CPointer] to the [wasmtime_context_t] representing the store.
 * @property func The [CPointer] to the [wasmtime_func_t] representing the function.
 *
 * @constructor Creates a new [Func] instance by providing the store and function pointers.
 * @param store A [CPointer] to the [wasmtime_context_t] representing the store.
 * @param func A [CPointer] to the [wasmtime_func_t] representing the function.
 */
class Func(
    store: CPointer<wasmtime_context_t>,
    val func: CPointer<wasmtime_func_t>
) : Extern(store, Extern.Kind.FUNC) {

    /**
     * Lazily retrieves the [FuncType] of the WebAssembly function.
     */
    val type: FuncType by lazy {
        val ptr = wasmtime_func_type(this.store, func) ?: throw Exception("failed to get function type")
        FuncType(ptr)
    }

    /**
     * Creates a new [Func] instance by providing the store, function type, and callback.
     *
     * @param store A [Store] instance associated with this function.
     * @param type The [FuncType] describing the function signature.
     * @param callback A [FuncCallback] to be called when the function is invoked.
     */
    constructor(
        store: Store<*>,
        type: FuncType,
        callback: FuncCallback,
    ) : this(
        store.context.context,
        nativeHeap.alloc<wasmtime_func_t>().apply {
            val cFuncType = FuncType.allocateCValue(type)
            val callbackRef = StableRef.create(callback)
            val cCallback: wasmtime_func_callback_t = staticCFunction(::cFuncCallback)
            val envFinalizer: CPointer<CFunction<(COpaquePointer?) -> Unit>> = staticCFunction { ptr: COpaquePointer? ->
                ptr?.asStableRef<FuncCallback>()?.dispose()
            }
            wasmtime_func_new(
                store.context.context,
                cFuncType,
                cCallback,
                callbackRef.asCPointer(),
                envFinalizer,
                this.ptr
            )
            FuncType.deleteCValue(cFuncType)
            store.own(this.ptr)
        }.ptr
    )

    /**
     * Calls this WebAssembly function with the given [args].
     *
     * @param args A list of [Val] arguments to pass to the function. Defaults to `null`.
     * @return A list of [Val] representing the results of the function call.
     * @throws IllegalArgumentException If the number of provided arguments does not match the number of function parameter types.
     * @throws [WasmtimeException] If an error occurs during the function call due to user error (e.g., wrong argument types or wrong store).
     * @throws Trap If a WebAssembly trap occurs during the function execution.
     */
    fun call(args: List<Val>? = null): List<Val> = memScoped {
        val paramTypes = type.params
        val resultTypes = type.results

        val nargs = args?.size ?: 0
        require((nargs) == paramTypes.size) {
            "expected ${paramTypes.size} arguments, got $nargs"
        }

        val cParams: CArrayPointer<wasmtime_val_t> = allocArray(nargs)
        args?.forEachIndexed { i, arg ->
            val cArg = Val.allocateCValue(arg)
            memcpy(cParams[i].ptr, cArg, sizeOf<wasmtime_val_t>().convert())
            Val.deleteCValue(cArg)
        }

        val cResults: CArrayPointer<wasmtime_val_t> = allocArray(resultTypes.size)

        val trap = allocPointerTo<wasm_trap_t>()
        val error = wasmtime_func_call(
            store,
            func,
            cParams,
            paramTypes.size.convert(),
            cResults,
            resultTypes.size.convert(),
            trap.ptr
        )

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

        /**
         * Creates a [Func] instance from a raw `size_t` value and a [Store].
         *
         * @param raw The raw `size_t` value representing the Wasmtime function.
         * @param store A [Store] instance representing the Wasmtime store.
         * @return A [Func] instance wrapping the Wasmtime function.
         */
        fun fromRaw(raw: size_t, store: Store<*>): Func {
            val ret = nativeHeap.alloc<wasmtime_func_t>()
            wasmtime_func_from_raw(store.context.context, raw, ret.ptr)
            return Func(store.context.context, ret.ptr)
        }
    }

    /**
     * Converts a [Func] instance into a raw `size_t` value.
     *
     * @return A raw `size_t` value representing the Wasmtime function.
     */
    fun toRaw(): size_t {
        return wasmtime_func_to_raw(store, func)
    }
}

/**
 * A low-level C callback function used by the [Func] class to wrap a [FuncCallback] for calling WebAssembly functions.
 *
 * This function is not intended for direct use in typical Kotlin code.
 * Instead, use the higher-level [Func] class for working with WebAssembly functions.
 *
 * @param cEnv A pointer to the environment holding the [FuncCallback] object.
 * @param cCaller A pointer to the [wasmtime_caller_t] object representing the WebAssembly caller.
 * @param cArgs A pointer to the array of [wasmtime_val_t] objects representing the function arguments.
 * @param nargs The number of arguments passed to the function.
 * @param cResults A pointer to the array of [wasmtime_val_t] objects where the results will be written.
 * @param nresults The number of results expected from the function call.
 * @return A pointer to a [wasm_trap_t] if a trap occurs during the function call, or `null` if the function call is successful.
 */
fun cFuncCallback(
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
        cResults?.let {
            val wasmtimeValSize = sizeOf<wasmtime_val_t>()
            for (i in 0 until nresults.toInt()) {
                val cVal = Val.allocateCValue(results[i])
                memcpy(cResults[i].ptr, cVal, wasmtimeValSize.convert())
                Val.deleteCValue(cVal)
            }
        }
        return null
    } else {
        val error = callbackResult.exceptionOrNull()!!
        val message = error.message ?: "unknown error during Wasm function call"
        return wasmtime_trap_new(message, message.length.convert())
    }
}

