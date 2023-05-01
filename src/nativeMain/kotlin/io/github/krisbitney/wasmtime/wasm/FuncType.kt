package io.github.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import io.github.krisbitney.wasmtime.util.toList
import wasmtime.*

/**
 * Represents the type of a WebAssembly function, containing information about the
 * parameter and result types of the function.
 *
 * @property params An array of [ValType] representing the parameter types of the function.
 * @property results An array of [ValType] representing the result types of the function.
 */
data class FuncType(
    val params: Array<ValType<*>>,
    val results: Array<ValType<*>>
) : ExternType(ExternType.Kind.FUNC) {

    /**
     * Constructs a new [FuncType] instance from a C pointer to a `wasm_functype_t`.
     *
     * @param funcType The C pointer to the `wasm_functype_t`.
     * @param ownedByCaller Whether the [funcType] pointer is owned by the caller and should not be deleted.
     * @throws Exception If the parameter or result type retrieval fails.
     */
    constructor(funcType: CPointer<wasm_functype_t>, ownedByCaller: Boolean = false) : this(
        funcType.let {
            val params = wasm_functype_params(funcType)
                ?: throw Exception("Failed to get params from wasm_functype_t.")
            params.toList().toTypedArray()
        },
        funcType.let {
            val results = wasm_functype_results(funcType)
                ?: throw Exception("Failed to get results from wasm_functype_t.")
            results.toList().toTypedArray()
        }
    ) {
        if (!ownedByCaller) wasm_functype_delete(funcType)
    }

    /**
     * Companion object providing utility methods for working with C values and pointers
     * related to the [FuncType] class.
     */
    internal companion object {
        /**
         * Allocates a new C pointer for the given [FuncType] and creates a `wasm_functype_t` instance.
         *
         * @param funcType The [FuncType] to be used for creating the `wasm_functype_t`.
         * @return The newly created C pointer to a `wasm_functype_t`.
         * @throws Exception If the creation of the `wasm_functype_t` fails.
         */
        fun allocateCValue(funcType: FuncType): CPointer<wasm_functype_t> = memScoped {
            val paramTypes = funcType.params
            val resultTypes = funcType.results
            val nParams = paramTypes.size
            val nResults = resultTypes.size
            val cParams = nativeHeap.alloc<wasm_valtype_vec_t>()
            val cResults = nativeHeap.alloc<wasm_valtype_vec_t>()

            memScoped {
                if (nParams > 0) {
                    val ps = allocArray<CPointerVar<wasm_valtype_t>>(nParams)
                    for (i in 0 until nParams) {
                        ps[i] = ValType.allocateCValue(paramTypes[i].kind)
                    }
                    wasm_valtype_vec_new(cParams.ptr, nParams.convert(), ps)
                } else {
                    wasm_valtype_vec_new_empty(cParams.ptr)
                }

                if (nResults > 0) {
                    val rs = allocArray<CPointerVar<wasm_valtype_t>>(nResults)
                    for (i in 0 until nResults) {
                        rs[i] = ValType.allocateCValue(resultTypes[i].kind)
                    }
                    wasm_valtype_vec_new(cResults.ptr, nResults.convert(), rs)
                } else {
                    wasm_valtype_vec_new_empty(cResults.ptr)
                }
            }

            wasm_functype_new(cParams.ptr, cResults.ptr) ?: throw Exception("Failed to create wasm_functype_t.")
        }

        /**
         * Deletes the C value for the given `wasm_functype_t` pointer.
         *
         * @param funcType The C pointer to the `wasm_functype_t` to be deleted.
         */
        fun deleteCValue(funcType: CPointer<wasm_functype_t>) {
            wasm_functype_delete(funcType)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !== null && this::class != other::class) return false

        other as FuncType

        if (!params.contentEquals(other.params)) return false
        return results.contentEquals(other.results)
    }

    override fun hashCode(): Int {
        var result = params.contentHashCode()
        result = 31 * result + results.contentHashCode()
        return result
    }
}
