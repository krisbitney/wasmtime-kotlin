package eth.krisbitney.wasmtime

import kotlinx.cinterop.CPointer
import wasmtime.wasmtime_func_t

/**
 * Represents a WebAssembly function reference.
 *
 * @property funcref A pointer to the wasmtime function reference.
 */
data class FuncRef(val funcref: CPointer<wasmtime_func_t>) {

    /**
     * Constructs a new [FuncRef] from the specified [func].
     *
     * @param func The [Func] to create the [FuncRef] from.
     */
    constructor(func: Func): this(func.func)

    /**
     * Creates a [Func] from this [FuncRef].
     * The store must be the same store that the [FuncRef] was created in.
     *
     * @param store The [Store] to create the [Func] within.
     * @return A [Func] created from this [FuncRef].
     */
    fun toFunc(store: Store<*>): Func = Func(store.context.context, funcref)
}