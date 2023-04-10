package eth.krisbitney.wasmtime.util

import eth.krisbitney.wasmtime.WasmtimeException
import kotlinx.cinterop.*
import wasmtime.*

fun wat2Wasm(wat: String): Result<ByteArray> = memScoped {
    val cBytes: CValuesRef<wasm_byte_vec_t> = cValue<wasm_byte_vec_t>() {}
    val error = wasmtime_wat2wasm(wat, wat.length.convert(), cBytes)
    if (error != null) {
        return Result.failure(WasmtimeException(error))
    }
    val bytesPtr = cBytes.getPointer(this)
    val size: Int = bytesPtr.pointed.size.toInt()
    val bytes: ByteArray? = bytesPtr.pointed.data?.readBytes(size)
    if (bytes == null) {
        Result.failure(Exception("Failed to convert wat to wasm. Byte vector is null."))
    } else {
        Result.success(bytes)
    }
}