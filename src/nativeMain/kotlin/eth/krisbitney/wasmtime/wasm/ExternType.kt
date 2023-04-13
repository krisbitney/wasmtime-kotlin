package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

/**
 * Represents the type of an external WebAssembly value. This sealed class can be seen as a superclass
 * of [FuncType], [GlobalType], [TableType], and [MemoryType], which correspond to the respective
 * WebAssembly extern types.
 *
 * @property kind The [Kind] of the external WebAssembly value type.
 */
sealed class ExternType(val kind: Kind) {

    companion object {
        /**
         * Constructs an [ExternType] subclass from a C pointer [externType] of type [wasm_externtype_t].
         * The [externType] pointer is owned by the subclass pointer and will be deleted by the subclass.
         *
         * @param externType The C pointer to a [wasm_externtype_t] object.
         * @return An [ExternType] subclass representing the external value type.
         */
        fun fromCValue(externType: CPointer<wasm_externtype_t>): ExternType {
            val kind = Kind.fromValue(wasm_externtype_kind(externType))
            return when (kind) {
                Kind.FUNC -> FuncType(wasm_externtype_as_functype(externType)!!)
                Kind.GLOBAL -> GlobalType(wasm_externtype_as_globaltype(externType)!!)
                Kind.TABLE -> TableType(wasm_externtype_as_tabletype(externType)!!)
                Kind.MEMORY -> MemoryType(wasm_externtype_as_memorytype(externType)!!)
            }
        }
    }

    /**
     * Represents the kind of external WebAssembly value type.
     *
     * @property value The integer value representing the [wasm_externkind_t] in the C API.
     */
    enum class Kind(val value: wasm_externkind_t) {
        FUNC(wasm_externkind_enum.WASM_EXTERN_FUNC.value.toUByte()),
        GLOBAL(wasm_externkind_enum.WASM_EXTERN_GLOBAL.value.toUByte()),
        TABLE(wasm_externkind_enum.WASM_EXTERN_TABLE.value.toUByte()),
        MEMORY(wasm_externkind_enum.WASM_EXTERN_MEMORY.value.toUByte());

        companion object {
            /**
             * Constructs a [Kind] object from the given [value] of type [wasm_externkind_t].
             *
             * @param value The [wasm_externkind_t] value representing the external value kind.
             * @return A [Kind] object corresponding to the given [value].
             * @throws IllegalArgumentException If the given [value] is not a valid [wasm_externkind_t].
             */
            fun fromValue(value: wasm_externkind_t): Kind {
                return values().find { it.value == value }  ?: throw IllegalArgumentException("Invalid ExternKind value: $value")
            }
        }
    }
}
