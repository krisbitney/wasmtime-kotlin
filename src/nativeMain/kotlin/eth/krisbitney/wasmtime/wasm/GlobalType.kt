package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*


/**
 * Represents the type of a WebAssembly global variable, containing information about the
 * content type and mutability of the global variable.
 *
 * @property content The [ValType.Kind] describing the content type of the global variable.
 * @property mutability The [Mutability] describing whether the global variable is mutable or immutable.
 *
 * @constructor Constructs a new [GlobalType] instance from a C pointer to a `wasm_globaltype_t`.
 * @param globalType The C pointer to the `wasm_globaltype_t`.
 * @throws RuntimeException If the content type or mutability retrieval fails.
 */
class GlobalType(
    val content: ValType.Kind,
    val mutability: Mutability
) : ExternType(ExternType.Kind.GLOBAL) {

    constructor(globalType: CPointer<wasm_globaltype_t>) : this(
        globalType.let {
            val result = wasm_globaltype_content(globalType)  ?: throw RuntimeException("Unable to get globaltype content")
            ValType.kindFromCValue(result)
        },
        globalType.let {
            val result = wasm_globaltype_mutability(globalType)
            Mutability.fromValue(result) ?: throw RuntimeException("Unable to get globaltype mutability")
        }
    ) {
        wasm_globaltype_delete(globalType)
    }

    /**
     * Companion object providing utility methods for working with C values and pointers
     * related to the [GlobalType] class.
     */
    companion object {
        /**
         * Allocates a new C pointer for the given [GlobalType] and creates a `wasm_globaltype_t` instance.
         *
         * @param globalType The [GlobalType] to be used for creating the `wasm_globaltype_t`.
         * @return The newly created C pointer to a `wasm_globaltype_t`.
         * @throws Error If there is a failure to create the global type.
         */
        fun allocateCValue(globalType: GlobalType): CPointer<wasm_globaltype_t> {
            val content = globalType.content
            val mutability = globalType.mutability
            return wasm_globaltype_new(ValType.allocateCValue(content), mutability.wasmMutability)
                ?: throw Exception("failed to create global type")
        }

        /**
         * Deletes the C value for the given `wasm_globaltype_t` pointer.
         *
         * @param globalType The C pointer to the `wasm_globaltype_t` to be deleted.
         */
        fun deleteCValue(globalType: CPointer<wasm_globaltype_t>) {
            wasm_globaltype_delete(globalType)
        }
    }
}
