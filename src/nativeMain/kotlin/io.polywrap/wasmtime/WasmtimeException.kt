package io.polywrap.wasmtime

import kotlinx.cinterop.*
import wasmtime.*

class WasmtimeException(private val error: CPointer<wasmtime_error_t>) : Throwable() {

    override val message: String = memScoped {
            val message = alloc<wasm_name_t>()
            wasmtime_error_message(error, message.ptr)
            val result = message.data?.toKString() ?: ""
            wasm_byte_vec_delete(message.ptr)
            result
        }

    val exitStatus: Int? = memScoped {
        val status = alloc<IntVar>()
        val hasStatus = wasmtime_error_exit_status(error, status.ptr)
        if (hasStatus) status.value else null
    }

    // TODO: create trace as String so that WasmFrame resources aren't left open
//    fun wasmTrace(): List<WasmFrame> = memScoped {
//        val frameVec = alloc<wasm_frame_vec_t>()
//        wasmtime_error_wasm_trace(error, frameVec.ptr)
//
//        val frames = List(frameVec.size.toInt()) { index ->
//            WasmFrame(frameVec.data?.get(index)!!)
//        }
//
//        wasm_frame_vec_delete(frameVec.ptr)
//        return frames
//    }

    init {
        wasmtime_error_delete(error)
    }
}