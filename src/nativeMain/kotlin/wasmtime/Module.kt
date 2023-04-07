package wasmtime

import kotlinx.cinterop.*
import toList
import wasm.*

@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
class Module(val module: CPointer<wasmtime_module_t>) : AutoCloseable {

    companion object {
        fun new(engine: Engine, wasm: ByteArray): Module = memScoped {
            val wasmBytes = wasm.toUByteArray().toCValues()
            val modulePtr = allocPointerTo<wasmtime_module_t>()

            wasmtime_module_new(engine.engine, wasmBytes, wasm.size.convert(), modulePtr.ptr).also { error ->
                error?.let { throw WasmtimeError(error) }
            }

            return Module(modulePtr.value!!)
        }

        fun validate(engine: Engine, wasm: ByteArray): Result<Unit> {
            val wasmBytes = wasm.toUByteArray().toCValues()
            val error = wasmtime_module_validate(engine.engine, wasmBytes, wasm.size.convert())
            return if (error == null) {
                Result.success(Unit)
            } else {
                Result.failure(WasmtimeError(error))
            }
        }
    }

    fun imports(): List<WasmImportType> {
        val imports = cValue<wasm_importtype_vec_t> { }
        wasmtime_module_imports(module, imports)
        return imports.toList()
    }

    fun exports(): List<WasmExportType> {
        val exports = cValue<wasm_exporttype_vec_t> { }
        wasmtime_module_exports(module, exports)
        return exports.toList()
    }

    fun serialize(): ByteArray {
        val byteVec = cValue<wasm_byte_vec_t> { }
        wasmtime_module_serialize(module, byteVec).also { error ->
            error?.let { throw WasmtimeError(error) }
        }
        return byteVec.getBytes()
    }

    fun deserialize(engine: Engine, bytes: ByteArray): Module = memScoped {
        val bytesValues = bytes.toUByteArray().toCValues()
        val modulePtr = allocPointerTo<wasmtime_module_t>()

        wasmtime_module_deserialize(engine.engine, bytesValues, bytes.size.convert(), modulePtr.ptr).also { error ->
            error?.let { throw WasmtimeError(error) }
        }

        return Module(modulePtr.value!!)
    }

    override fun close() {
        wasmtime_module_delete(module)
    }
}
