package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class TableType(val tableType: CPointer<wasm_tabletype_t>) : AutoCloseable {

    constructor(elementType: ValType.Kind, min: UInt = 0u, max: UInt = WasmLimits.LIMITS_MAX_DEFAULT) :
            this(wasm_tabletype_new(ValType.allocateCValue(elementType), WasmLimits.cLimits(min, max).ptr)
                ?: throw Error("failed to create table type"))

    val element: ValType.Kind
        get() {
            val result = wasm_tabletype_element(tableType)  ?: throw RuntimeException("failed to get table type element")
            return ValType.kindFromCValue(result)
        }


    val limits: WasmLimits
        get() {
        val ptr = wasm_tabletype_limits(tableType) ?: throw Error("failed to get table type limits")
        return WasmLimits(ptr.pointed.min, ptr.pointed.max)
    }

    override fun close() {
        wasm_tabletype_delete(tableType)
    }
}
