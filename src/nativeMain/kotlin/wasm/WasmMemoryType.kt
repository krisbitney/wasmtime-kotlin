package wasm

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class WasmMemoryType(val memoryType: CPointer<wasm_memorytype_t>) : AutoCloseable {

    constructor(min: UInt = 0u, max: UInt = WasmLimits.LIMITS_MAX_DEFAULT) :
            this(wasm_memorytype_new(WasmLimits.cLimits(min, max).ptr) ?: throw Error("failed to create memory type"))

    val limits: WasmLimits get() {
        val ptr = wasm_memorytype_limits(memoryType) ?: throw Error("failed to get memory limits")
        return WasmLimits(ptr.pointed.min, ptr.pointed.max)
    }
    override fun close() {
        wasm_memorytype_delete(memoryType)
    }
}
