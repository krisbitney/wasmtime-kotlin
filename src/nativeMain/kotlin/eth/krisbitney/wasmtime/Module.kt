package eth.krisbitney.wasmtime

import eth.krisbitney.wasmtime.wasm.ExportType
import eth.krisbitney.wasmtime.wasm.ImportType
import kotlinx.cinterop.*
import toList
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
class Module(val module: CPointer<wasmtime_module_t>) : AutoCloseable {

    constructor(engine: Engine, wasm: ByteArray) :
            this(
                memScoped {
                    val wasmBytes = wasm.toUByteArray().toCValues()
                    val modulePtr = allocPointerTo<wasmtime_module_t>()

                    wasmtime_module_new(engine.engine, wasmBytes, wasm.size.convert(), modulePtr.ptr).also { error ->
                        error?.let { throw WasmtimeException(error) }
                    }

                    modulePtr.value!!
                }
            )

    constructor(engine: Engine, wat: String) : this(engine, wat2Wasm(wat).getOrThrow())

    companion object {
        fun validate(engine: Engine, wasm: ByteArray): Result<Unit> {
            val wasmBytes = wasm.toUByteArray().toCValues()
            val error = wasmtime_module_validate(engine.engine, wasmBytes, wasm.size.convert())
            return if (error == null) {
                Result.success(Unit)
            } else {
                Result.failure(WasmtimeException(error))
            }
        }

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
    }

    fun imports(): List<ImportType> {
        val imports = cValue<wasm_importtype_vec_t> { }
        wasmtime_module_imports(module, imports)
        val list = imports.toList()
        // TODO: `imports` is on the stack. Do I need to free its data pointer?
        imports.useContents { this.data?.let { nativeHeap.free(it) } }
        return list
    }

    fun exports(): List<ExportType> {
        val exports = cValue<wasm_exporttype_vec_t> { }
        wasmtime_module_exports(module, exports)
        val list = exports.toList()
        // TODO: `exports` is on the stack. Do I need to free its data pointer?
        exports.useContents { this.data?.let { nativeHeap.free(it) } }
        return list
    }

    fun serialize(): ByteArray {
        val byteVec = cValue<wasm_byte_vec_t> { }
        wasmtime_module_serialize(module, byteVec).also { error ->
            error?.let { throw WasmtimeException(error) }
        }
        return byteVec.getBytes()
    }

    fun deserialize(engine: Engine, bytes: ByteArray): Module = memScoped {
        val bytesValues = bytes.toUByteArray().toCValues()
        val modulePtr = allocPointerTo<wasmtime_module_t>()

        wasmtime_module_deserialize(engine.engine, bytesValues, bytes.size.convert(), modulePtr.ptr).also { error ->
            error?.let { throw WasmtimeException(error) }
        }

        return Module(modulePtr.value!!)
    }

    override fun close() {
        wasmtime_module_delete(module)
    }
}
