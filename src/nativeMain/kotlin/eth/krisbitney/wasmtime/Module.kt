package eth.krisbitney.wasmtime

import eth.krisbitney.wasmtime.wasm.ExportType
import eth.krisbitney.wasmtime.wasm.ImportType
import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.util.toList
import wasmtime.*

/**
 * A compiled WebAssembly module is ready to be instantiated and can be inspected for imports/exports.
 * It is safe to use a module across multiple threads simultaneously.
 *
 * @property module Pointer to the underlying `wasmtime_module_t` structure.
 * @constructor Creates a Module instance from the provided engine and wasm byte array.
 * @constructor Creates a Module instance from the provided engine and WebAssembly text format (WAT) string.
 */
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

                    modulePtr.value ?: throw Exception("Failed to create module.")
                }
            )

    constructor(engine: Engine, wat: String) : this(engine, wat2Wasm(wat).getOrThrow())

    companion object {
        /**
         * Validates a WebAssembly binary.
         *
         * @param engine The Engine instance to use for validation.
         * @param wasm ByteArray containing the WebAssembly binary.
         * @return Result<Unit> indicating success or failure of validation.
         */
        fun validate(engine: Engine, wasm: ByteArray): Result<Unit> {
            val wasmBytes = wasm.toUByteArray().toCValues()
            val error = wasmtime_module_validate(engine.engine, wasmBytes, wasm.size.convert())
            return if (error == null) {
                Result.success(Unit)
            } else {
                Result.failure(WasmtimeException(error))
            }
        }

        /**
         * Converts a WebAssembly text format (WAT) string to a WebAssembly binary ByteArray.
         *
         * @param wat String containing the WebAssembly text format (WAT).
         * @return Result<ByteArray> with the WebAssembly binary, or an error if conversion fails.
         */
        fun wat2Wasm(wat: String): Result<ByteArray> = memScoped {
            val cBytes: CValuesRef<wasm_byte_vec_t> = cValue<wasm_byte_vec_t>()
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

    /**
     * Returns a list of import types required by this module.
     *
     * @return [List] of [ImportType] instances required by this module.
     */
    fun imports(): List<ImportType> {
        val imports = cValue<wasm_importtype_vec_t>()
        wasmtime_module_imports(module, imports)
        val list = imports.toList()
        imports.useContents { this.data?.let { nativeHeap.free(it) } }
        return list
    }

    /**
     * Returns a list of export types provided by this module.
     *
     * @return [List] of [ExportType] instances provided by this module.
     */
    fun exports(): List<ExportType> {
        val exports = cValue<wasm_exporttype_vec_t>()
        wasmtime_module_exports(module, exports)
        val list = exports.toList()
        exports.useContents { this.data?.let { nativeHeap.free(it) } }
        return list
    }

    /**
     * Serializes the compiled module artifacts as a byte array.
     *
     * @return [ByteArray] representing the serialized compiled module.
     */
    fun serialize(): ByteArray {
        val byteVec = cValue<wasm_byte_vec_t>()
        wasmtime_module_serialize(module, byteVec).also { error ->
            error?.let { throw WasmtimeException(error) }
        }
        return byteVec.getBytes()
    }

    /**
     * Deserializes the provided byte array into a Module instance.
     *
     * @param engine The Engine instance to use for deserialization.
     * @param bytes ByteArray containing the serialized module data.
     * @return Module instance obtained from deserializing the input byte array.
     */
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
