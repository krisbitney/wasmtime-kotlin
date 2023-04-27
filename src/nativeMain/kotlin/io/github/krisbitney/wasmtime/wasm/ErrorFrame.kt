package io.github.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

/**
 * Represents a frame of a WebAssembly stack trace.
 *
 * @property funcIndex The function index in the original WebAssembly module that this frame corresponds to.
 * @property funcOffset The byte offset from the beginning of the function in the original WebAssembly file to the instruction this frame points to.
 * @property moduleOffset The byte offset from the beginning of the original WebAssembly file to the instruction this frame points to.
 * @property funcName The name of the function this frame corresponds to, if available.
 * @property moduleName The name of the module this frame corresponds to, if available.
 */
class ErrorFrame(
    val funcIndex: UInt,
    val funcOffset: ULong,
    val moduleOffset: ULong,
    val funcName: String? = null,
    val moduleName: String? = null
    ) {

    /**
     * Constructs a new [ErrorFrame] from the given [wasm_frame_t] pointer.
     *
     * @param frame The C pointer to a [wasm_frame_t] struct.
     */
    constructor(frame: CPointer<wasm_frame_t>) : this(
        wasm_frame_func_index(frame),
        wasm_frame_func_offset(frame),
        wasm_frame_module_offset(frame),
        wasmtime_frame_func_name(frame)?.let { it.pointed.data?.toKString() },
        wasmtime_frame_module_name(frame)?.let { it.pointed.data?.toKString() }
    ) {
        wasm_frame_delete(frame)
    }

    override fun toString(): String {
        return "WasmStackTrace(funcIndex=$funcIndex, funcOffset=$funcOffset, moduleOffset=$moduleOffset, funcName=$funcName, moduleName=$moduleName)"
    }
}
