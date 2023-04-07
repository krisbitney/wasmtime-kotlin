package wasm
import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class WasmTableType(val tableType: CPointer<wasm_tabletype_t>) : AutoCloseable {

    constructor(elementType: WasmValType, min: UInt = 0u, max: UInt = WasmLimits.LIMITS_MAX_DEFAULT) :
            this(wasm_tabletype_new(elementType.valType, WasmLimits.cLimits(min, max).ptr) ?: throw Error("failed to create table type"))

    val element: WasmValType = WasmValType(wasm_tabletype_element(tableType) ?: throw Error("failed to get table type element"))

    val limits: WasmLimits get() {
        val ptr = wasm_tabletype_limits(tableType) ?: throw Error("failed to get table type limits")
        return WasmLimits(ptr.pointed.min, ptr.pointed.max)
    }

    override fun close() {
        wasm_tabletype_delete(tableType)
    }
}
