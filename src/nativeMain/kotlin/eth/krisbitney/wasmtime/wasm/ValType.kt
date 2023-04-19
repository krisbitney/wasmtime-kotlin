package eth.krisbitney.wasmtime.wasm

import eth.krisbitney.wasmtime.*
import kotlinx.cinterop.*
import wasmtime.*

/**
 * A sealed class representing the type of a WebAssembly value.
 *
 * @param T The type of the underlying value.
 * @property kind The [Kind] of this value type.
 * @property isNum Indicates whether this value type is a numeric type.
 * @property isRef Indicates whether this value type is a reference type.
 */
sealed class ValType<T : Any>(val kind: Kind) {

    val isNum: Boolean = wasm_valkind_is_num(kind.value.toUByte())

    val isRef: Boolean = !isNum

    /**
     * Converts a [Val] instance into a value of type [T].
     *
     * @param value The [Val] to convert.
     * @return The converted value of type [T].
     */
    abstract fun fromVal(value: Val): T

    /**
     * A [ValType] representing a 32-bit integer WebAssembly value.
     */
    class I32() : ValType<Int>(Kind.I32) {
        /**
         * Converts a [Val] instance into a 32-bit integer value.
         *
         * @param value The [Val] to convert.
         * @return The converted 32-bit integer value.
         */
        override fun fromVal(value: Val): Int {
            return value.i32
        }
    }

    /**
     * A [ValType] representing a 64-bit integer WebAssembly value.
     */
    class I64() : ValType<Long>(Kind.I64) {
        /**
         * Converts a [Val] instance into a 64-bit integer value.
         *
         * @param value The [Val] to convert.
         * @return The converted 64-bit integer value.
         */
        override fun fromVal(value: Val): Long {
            return value.i64
        }
    }

    /**
     * A [ValType] representing a 32-bit floating-point WebAssembly value.
     */
    class F32() : ValType<Float>(Kind.F32) {
        /**
         * Converts a [Val] instance into a 32-bit floating-point value.
         *
         * @param value The [Val] to convert.
         * @return The converted 32-bit floating-point value.
         */
        override fun fromVal(value: Val): Float {
            return value.f32
        }
    }

    /**
     * A [ValType] representing a 64-bit floating-point WebAssembly value.
     */
    class F64() : ValType<Double>(Kind.F64) {
        /**
         * Converts a [Val] instance into a 64-bit floating-point value.
         *
         * @param value The [Val] to convert.
         * @return The converted 64-bit floating-point value.
         */
        override fun fromVal(value: Val): Double {
            return value.f64
        }
    }
    /**
     * A [ValType] representing a WebAssembly reference to an external resource.
     */
    class AnyRef() : ValType<ExternRef<*>>(Kind.ANYREF) {
        /**
         * Converts a [Val] instance into an [ExternRef] instance.
         *
         * @param value The [Val] to convert.
         * @return The converted [ExternRef] instance.
         */
        override fun fromVal(value: Val): ExternRef<*> {
            return value.externref
        }
    }
    /**
     * A [ValType] representing a WebAssembly function reference.
     */
    class FuncRefType() : ValType<FuncRef>(Kind.FUNCREF) {
        /**
         * Converts a [Val] instance into a [FuncRef] representing a WebAssembly function reference.
         *
         * @param value The [Val] to convert.
         * @return The converted [FuncRef].
         */
        override fun fromVal(value: Val): FuncRef {
            return value.funcref
        }
    }

    /**
     * Companion object containing utility methods to create and manage [ValType] instances
     * and their associated C values (of type [wasm_valtype_t]).
     */
    companion object {
        /**
         * Creates a [ValType] instance from the given [Kind].
         *
         * @param kind The [Kind] representing a WebAssembly value type.
         * @return A [ValType] instance corresponding to the input [Kind].
         */
        fun fromKind(kind: Kind): ValType<*> {
            return when (kind) {
                Kind.I32 -> I32()
                Kind.I64 -> I64()
                Kind.F32 -> F32()
                Kind.F64 -> F64()
                Kind.ANYREF -> AnyRef()
                Kind.FUNCREF -> FuncRefType()
            }
        }

        /**
         * Creates a [ValType] instance from the given C value.
         *
         * @param valType A pointer to a [wasm_valtype_t] representing a WebAssembly value type.
         * @return A [ValType] instance corresponding to the input C value.
         */
        fun fromCValue(valType: CPointer<wasm_valtype_t>): ValType<*> {
            val kind = Kind.fromInt(wasm_valtype_kind(valType).toInt())
            return when (kind) {
                Kind.I32 -> I32()
                Kind.I64 -> I64()
                Kind.F32 -> F32()
                Kind.F64 -> F64()
                Kind.ANYREF -> AnyRef()
                Kind.FUNCREF -> FuncRefType()
            }
        }

        /**
         * Returns the [Kind] associated with the given C value.
         *
         * @param valType A pointer to a [wasm_valtype_t] representing a WebAssembly value type.
         * @return The [Kind] instance corresponding to the input C value.
         */
        fun kindFromCValue(valType: CPointer<wasm_valtype_t>): Kind {
            return Kind.fromInt(wasm_valtype_kind(valType).toInt())
        }

        /**
         * Deletes a C value of type [wasm_valtype_t].
         *
         * @param valType A pointer to a [wasm_valtype_t] representing a WebAssembly value type.
         */
        fun deleteCValue(valType: CPointer<wasm_valtype_t>) {
            wasm_valtype_delete(valType)
        }

        /**
         * Allocates a new C value of type [wasm_valtype_t] with the specified [Kind].
         *
         * @param kind The [Kind] representing a WebAssembly value type.
         * @return A pointer to the allocated [wasm_valtype_t] corresponding to the input [Kind].
         * @throws Error If the allocation fails.
         */
        fun allocateCValue(kind: Kind): CPointer<wasm_valtype_t> {
            return wasm_valtype_new(kind.value.toUByte()) ?: throw Exception("failed to create wasm_valtype")
        }
    }

    /**
     * Enum class representing the different kinds of value types supported in WebAssembly.
     *
     * Each enum constant corresponds to a specific WebAssembly value type, such as `I32`, `I64`, `F32`,
     * `F64`, `ANYREF`, and `FUNCREF`. These types are used to define the data types of WebAssembly
     * module functions' parameters and return values, as well as global and local variables.
     *
     * @property value The integer value associated with this value type.
     */
    enum class Kind(val value: Int) {
        /**
         * Represents the 32-bit integer WebAssembly value type.
         */
        I32(WASM_I32.toInt()),

        /**
         * Represents the 64-bit integer WebAssembly value type.
         */
        I64(WASM_I64.toInt()),

        /**
         * Represents the 32-bit floating-point WebAssembly value type.
         */
        F32(WASM_F32.toInt()),

        /**
         * Represents the 64-bit floating-point WebAssembly value type.
         */
        F64(WASM_F64.toInt()),

        /**
         * Represents the WebAssembly value type for any reference type.
         */
        ANYREF(WASM_ANYREF.toInt()),

        /**
         * Represents the WebAssembly value type for function references.
         */
        FUNCREF(WASM_FUNCREF.toInt());

        companion object {
            private val map = values().associateBy(Kind::value)
            /**
             * Returns a [Kind] instance corresponding to the given integer value.
             *
             * @param value The integer value representing a WebAssembly value type.
             * @return The [Kind] instance corresponding to the input integer value.
             * @throws IllegalArgumentException If the given integer value does not correspond to any valid WebAssembly value type.
             */
            fun fromInt(value: Int) = map[value] ?: error("Invalid WasmValKind value: $value")
        }
    }
}
