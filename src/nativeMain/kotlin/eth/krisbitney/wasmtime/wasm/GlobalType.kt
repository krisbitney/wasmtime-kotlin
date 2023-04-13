package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

/**
 * Represents a global type in WebAssembly.
 *
 * @property globalType The C pointer to the `wasm_globaltype_t` struct.
 *
 * @constructor Creates a new [GlobalType] instance with the given content type and mutability.
 * @param content The [ValType.Kind] representing the content type of the global.
 * @param mutability The [Mutability] indicating whether the global is mutable or not.
 *
 * @throws Error If there is a failure to create the global type.
 */
@OptIn(ExperimentalStdlibApi::class)
class GlobalType(val globalType: CPointer<wasm_globaltype_t>) : AutoCloseable {

    /**
     * Creates a new [GlobalType] instance from the given content type and mutability.
     *
     * @param content The [ValType.Kind] representing the content type of the global.
     * @param mutability The [Mutability] indicating whether the global is mutable or not.
     */
    constructor(content: ValType.Kind, mutability: Mutability) :
            this(wasm_globaltype_new(ValType.allocateCValue(content), mutability.wasmMutability)
                ?: throw Error("failed to create global type"))

    /**
     * The content type of the global, represented as a [ValType.Kind].
     */
    val content: ValType.Kind
        get() {
            val result = wasm_globaltype_content(globalType)  ?: throw RuntimeException("Unable to get globaltype content")
            return ValType.kindFromCValue(result)
        }

    /**
     * The mutability of the global, represented as a [Mutability] instance.
     */
    val mutability: Mutability
        get() {
            val result = wasm_globaltype_mutability(globalType)
            return Mutability.fromValue(result) ?: throw RuntimeException("Unable to get globaltype mutability")
        }

    override fun close() {
        nativeHeap.free(globalType)
    }
}
