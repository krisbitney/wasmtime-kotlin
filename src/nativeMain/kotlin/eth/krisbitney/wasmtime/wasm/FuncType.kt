package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import toList
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class FuncType(val funcType: CPointer<wasm_functype_t>) : AutoCloseable {

    constructor(
        paramTypes: Array<ValType<*>>? = null,
        resultTypes: Array<ValType<*>>? = null
    ): this(paramTypes.run {
        val nParams = paramTypes?.size ?: 0
        val nResults = resultTypes?.size ?: 0
        val cParams = cValue<wasm_valtype_vec_t>()
        val cResults = cValue<wasm_valtype_vec_t>()

        memScoped {
            if (paramTypes !== null && nParams > 0) {
                val ps = allocArray<CPointerVar<wasm_valtype_t>>(nParams)
                for (i in 0 until nParams) {
                    ps[i] = ValType.allocateCValue(paramTypes[i].kind)
                }
                wasm_valtype_vec_new(cParams, nParams.convert(), ps)
                for (i in 0 until nParams) {
                    ValType.deleteCValue(ps[i]!!)
                }
            } else {
                wasm_valtype_vec_new_empty(cParams)
            }

            if (resultTypes !== null && nResults > 0) {
                val rs = allocArray<CPointerVar<wasm_valtype_t>>(nResults)
                for (i in 0 until nResults) {
                    rs[i] = ValType.allocateCValue(resultTypes[i].kind)
                }
                wasm_valtype_vec_new(cResults, nResults.convert(), rs)
                for (i in 0 until nResults) {
                    ValType.deleteCValue(rs[i]!!)
                }
            } else {
                wasm_valtype_vec_new_empty(cResults)
            }
        }

        val funcType: CPointer<wasm_functype_t> = wasm_functype_new(cParams, cResults)
            ?: throw Exception("Failed to create wasm_functype_t.")
        funcType
    })

    /** returned WasmValTypes are owned by WasmFuncType. Caller should not deallocate them. */
    fun params(): List<ValType<*>> {
        val params = wasm_functype_params(funcType)
            ?: throw Exception("Failed to get params from wasm_functype_t.")
        return params.toList()
    }

    /** returned WasmValTypes are owned by WasmFuncType. Caller should not deallocate them. */
    fun results(): List<ValType<*>> {
        val results = wasm_functype_results(funcType)
            ?: throw Exception("Failed to get results from wasm_functype_t.")
        return results.toList()
    }

    override fun close() {
        wasm_functype_delete(funcType)
    }
}
