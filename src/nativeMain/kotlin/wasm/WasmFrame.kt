package wasm

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class WasmFrame(val frame: CPointer<wasm_frame_t>) : AutoCloseable {
    fun copy(): WasmFrame {
        val newFrame = wasm_frame_copy(frame) ?: throw RuntimeException("Failed to copy wasm_frame_t")
        return WasmFrame(newFrame)
    }

    // TODO: should I wrap wasm_instance_t?
    val instance // : CPointer<wasm_instance_t>?
        get() = wasm_frame_instance(frame)

    val funcIndex: UInt
        get() = wasm_frame_func_index(frame)

    val funcOffset: ULong
        get() = wasm_frame_func_offset(frame)

    val moduleOffset: ULong
        get() = wasm_frame_module_offset(frame)

    override fun close() {
        wasm_frame_delete(frame)
    }
}
