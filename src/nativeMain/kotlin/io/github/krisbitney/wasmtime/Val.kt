package io.github.krisbitney.wasmtime

import io.github.krisbitney.wasmtime.wasm.ValType
import kotlinx.cinterop.*
import platform.posix.memcpy
import wasmtime.*

/**
 * Represents a WebAssembly value in the Wasmtime environment.
 *
 * @property kind The [Kind] of this value, representing its type.
 * @property value The actual value of the appropriate type.
 */
data class Val(val kind: Kind, val value: Any) {

    /**
     * Creates a [Val] instance representing an `i32` WebAssembly value.
     *
     * @param value The `Int` value to store.
     */
    constructor(value: Int) : this(Kind.I32, value)

    /**
     * Creates a [Val] instance representing an `i64` WebAssembly value.
     *
     * @param value The `Long` value to store.
     */
    constructor(value: Long) : this(Kind.I64, value)

    /**
     * Creates a [Val] instance representing an `f32` WebAssembly value.
     *
     * @param value The `Float` value to store.
     */
    constructor(value: Float) : this(Kind.F32, value)

    /**
     * Creates a [Val] instance representing an `f64` WebAssembly value.
     *
     * @param value The `Double` value to store.
     */
    constructor(value: Double) : this(Kind.F64, value)

    /**
     * Creates a [Val] instance representing a `v128` WebAssembly value.
     *
     * @param value The `wasmtime_v128` value to store.
     */
    constructor(value: wasmtime_v128) : this(Kind.V128, value)

    /**
     * Creates a [Val] instance representing a [FuncRef] WebAssembly value.
     *
     * @param value The [FuncRef] value to store.
     */
    constructor(value: FuncRef) : this(Kind.FUNCREF, value)

    /**
     * Creates a [Val] instance representing an `externref` WebAssembly value.
     *
     * @param value The [ExternRef] value to store.
     */
    constructor(value: ExternRef<*>) : this(Kind.EXTERNREF, value)

    /**
     * Creates a [Val] instance for the given [ValType] and value.
     *
     * @param valType The [ValType] representing the type of the value.
     * @param value The value of the appropriate type.
     * @throws IllegalArgumentException If the provided value does not match the expected type.
     */
    constructor(valType: ValType<*>, value: Any) : this(Kind.fromValType(valType), value) {
        if (valType is ValType.I32 && value !is Int) {
            throw IllegalArgumentException("Expected Int for I32")
        } else if (valType is ValType.I64 && value !is Long) {
            throw IllegalArgumentException("Expected Long for I64")
        } else if (valType is ValType.F32 && value !is Float) {
            throw IllegalArgumentException("Expected Float for F32")
        } else if (valType is ValType.F64 && value !is Double) {
            throw IllegalArgumentException("Expected Double for F64")
        } else if (valType is ValType.FuncRefType && value !is FuncRef) {
            throw IllegalArgumentException("Expected FuncRef for FuncRef")
        } else if (valType is ValType.AnyRef && value !is ExternRef<*>) {
            throw IllegalArgumentException("Expected ExternRef<*> for AnyRef")
        }
    }

    /**
     * Retrieves the `Int` value of this [Val] instance.
     *
     * @throws IllegalStateException If the [kind] is not [Kind.I32].
     */
    val i32: Int
        get() {
            if(kind != Kind.I32) throw IllegalStateException("Value is not i32")
            return value as Int
        }

    /**
     * Retrieves the `Long` value of this [Val] instance.
     *
     * @throws IllegalStateException If the [kind] is not [Kind.I64].
     */
    val i64: Long
        get() {
            if (kind != Kind.I64) throw IllegalStateException("Value is not i64")
            return value as Long
        }

    /**
     * Retrieves the `Float` value of this [Val] instance.
     *
     * @throws IllegalStateException If the [kind] is not [Kind.F32].
     */
    val f32: Float
        get() {
            if (kind != Kind.F32) throw IllegalStateException("Value is not f32")
            return value as Float
        }

    /**
     * Retrieves the `Double` value from this [Val] instance.
     *
     * @throws IllegalStateException If the [kind] is not [Kind.F64].
     */
    val f64: Double
        get() {
            if (kind != Kind.F64) throw IllegalStateException("Value is not f64")
            return value as Double
        }

    /**
     * Retrieves the `v128` value from this [Val] instance.
     *
     * @property v128 The `v128` value contained in this [Val].
     * @throws IllegalStateException If the [kind] is not [Kind.V128].
     */
    @Suppress("UNCHECKED_CAST")
    val v128: wasmtime_v128
        get() {
            if (kind != Kind.V128) throw IllegalStateException("Value is not v128")
            return value as? wasmtime_v128 ?: throw IllegalStateException("Value is not v128")
        }

    /**
     * Retrieves the function reference value from this [Val] instance.
     *
     * @property funcref The [FuncRef] value contained in this [Val].
     * @throws IllegalStateException If the [kind] is not [Kind.FUNCREF].
     */
    val funcref: FuncRef
        get() {
            if (kind != Kind.FUNCREF) throw IllegalStateException("Value is not funcref")
            return value as FuncRef
        }

    /**
     * Retrieves the extern reference value from this [Val] instance.
     *
     * @property externref The extern reference (`ExternRef<*>`) value contained in this [Val].
     * @throws IllegalStateException If the [kind] is not [Kind.EXTERNREF].
     */
    val externref: ExternRef<*>
        get() {
            if (kind != Kind.EXTERNREF) throw IllegalStateException("Value is not externref")
            return value as ExternRef<*>
        }

    /**
     * A companion object that provides utility functions for creating and working with [Val] instances.
     */
    internal companion object {
        /**
         * Creates a [Val] instance from a given [wasmtime_val_t] C pointer.
         *
         * @param value A C pointer to a [wasmtime_val_t] struct.
         * @return A [Val] instance representing the provided wasm value.
         * @throws IllegalArgumentException If the wasm value has an unsupported kind.
         */
        fun fromCValue(value: CPointer<wasmtime_val_t>): Val {
            return when (value.pointed.kind.toInt()) {
                Val.Kind.I32.value -> Val(value.pointed.of.i32)
                Val.Kind.I64.value -> Val(value.pointed.of.i64)
                Val.Kind.F32.value -> Val(value.pointed.of.f32)
                Val.Kind.F64.value -> Val(value.pointed.of.f64)
                Val.Kind.V128.value -> Val(value.pointed.of.v128)
                Val.Kind.FUNCREF.value -> Val(FuncRef(value.pointed.of.funcref.ptr))
                Val.Kind.EXTERNREF.value -> Val(ExternRef<Any>(value.pointed.of.externref!!))
                else -> throw IllegalArgumentException("Invalid WasmtimeValKind value: ${value.pointed.kind}")
            }
        }

        /**
         * Retrieves the [Kind] of a wasm value from a given [wasmtime_val_t] C pointer.
         *
         * @param wasmtimeVal A C pointer to a [wasmtime_val_t] struct.
         * @return A [Kind] representing the wasm value's kind.
         * @throws IllegalArgumentException If the wasm value has an unsupported kind.
         */
        fun kindFromCValue(wasmtimeVal: CPointer<wasmtime_val_t>): Kind {
            return Kind.fromValue(wasmtimeVal.pointed.kind.toInt())
        }

        /**
         * Deletes a [wasmtime_val_t] value, freeing its memory.
         * Does not free the memory of an [ExternRef] value contained in the [wasmtime_val_t].
         *
         * @param wasmtimeVal A C pointer to a [wasmtime_val_t] struct.
         */
        fun deleteCValue(wasmtimeVal: CPointer<wasmtime_val_t>) {
            if (wasmtimeVal.pointed.kind.toInt() == WASMTIME_FUNCREF) {
                nativeHeap.free(wasmtimeVal.pointed.of.funcref)
            } else if (wasmtimeVal.pointed.kind.toInt() == WASMTIME_V128) {
                nativeHeap.free(wasmtimeVal.pointed.of.v128)
            }
            wasmtime_val_delete(wasmtimeVal)
        }

        /**
         * Allocates a new [wasmtime_val_t] from the provided [Val] instance.
         *
         * @param value A [Val] instance representing a wasm value.
         * @return A C pointer to a newly allocated [wasmtime_val_t] struct representing the wasm value.
         */
        fun allocateCValue(value: Val): CPointer<wasmtime_val_t> {
            return when (value.kind) {
                Val.Kind.I32 -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = Val.Kind.I32.value.toUByte()
                    of.i32 = value.i32
                }.ptr
                Val.Kind.I64 -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = Val.Kind.I64.value.toUByte()
                    of.i64 = value.i64
                }.ptr
                Val.Kind.F32 -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = Val.Kind.F32.value.toUByte()
                    of.f32 = value.f32
                }.ptr
                Val.Kind.F64 -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = Val.Kind.F64.value.toUByte()
                    of.f64 = value.f64
                }.ptr
                Val.Kind.V128 -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = Val.Kind.V128.value.toUByte()
                    memcpy(of.v128, value.v128, 16)
                }.ptr
                Val.Kind.FUNCREF -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = Val.Kind.FUNCREF.value.toUByte()
                    memcpy(of.funcref.ptr, value.funcref.funcref, sizeOf<wasmtime_func_t>().toULong())
                }.ptr
                Val.Kind.EXTERNREF -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = Val.Kind.EXTERNREF.value.toUByte()
                    of.externref = value.externref.externRef
                }.ptr
            }
        }

        /**
         * Copies a [wasmtime_val_t], creating a new [wasmtime_val_t] C value with the same contents.
         *
         * @param wasmtimeVal A C pointer to a [wasmtime_val_t] struct.
         * @return A C pointer to a newly allocated [wasmtime_val_t] struct containing the same wasm value.
         */
        fun copyCVal(wasmtimeVal: CPointer<wasmtime_val_t>): CPointer<wasmtime_val_t> {
            val copy = nativeHeap.alloc<wasmtime_val_t>().ptr
            wasmtime_val_copy(copy, wasmtimeVal)
            return copy
        }
    }

    /**
     * An enumeration representing the various kinds of wasm values that can be stored in a [Val] instance.
     *
     * @property value The integer value associated with the wasm value kind.
     */
    enum class Kind(val value: Int) {
        I32(WASMTIME_I32),
        I64(WASMTIME_I64),
        F32(WASMTIME_F32),
        F64(WASMTIME_F64),
        V128(WASMTIME_V128),
        FUNCREF(WASMTIME_FUNCREF),
        EXTERNREF(WASMTIME_EXTERNREF);

        companion object {
            /**
             * Retrieves the [Kind] enumeration value corresponding to the provided integer [value].
             *
             * @param value The integer value representing a wasm value kind.
             * @return The [Kind] enumeration value corresponding to the provided integer value.
             * @throws IllegalArgumentException If the provided integer value does not correspond to a valid wasm value kind.
             */
            fun fromValue(value: Int): Kind {
                return values().find { it.value == value } ?: throw IllegalArgumentException("Invalid WasmtimeValKind value: $value")
            }

            /**
             * Retrieves the [Kind] enumeration value corresponding to the provided [ValType] instance.
             *
             * @param valType A [ValType] instance representing a wasm value type.
             * @return The [Kind] enumeration value corresponding to the provided [ValType] instance.
             */
            fun fromValType(valType: ValType<*>): Kind {
                return when (valType.kind) {
                    ValType.Kind.I32 -> I32
                    ValType.Kind.I64 -> I64
                    ValType.Kind.F32 -> F32
                    ValType.Kind.F64 -> F64
                    ValType.Kind.FUNCREF -> FUNCREF
                    ValType.Kind.ANYREF -> EXTERNREF
                }
            }
        }
    }
}
