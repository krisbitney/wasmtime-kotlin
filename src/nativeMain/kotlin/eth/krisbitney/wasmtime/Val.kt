package eth.krisbitney.wasmtime

import eth.krisbitney.wasmtime.wasm.ValType
import kotlinx.cinterop.*
import platform.posix.memcpy
import wasmtime.*

data class Val(val kind: Kind, val value: Any) {

    constructor(value: Int) : this(Kind.I32, value)
    constructor(value: Long) : this(Kind.I64, value)
    constructor(value: Float) : this(Kind.F32, value)
    constructor(value: Double) : this(Kind.F64, value)
    constructor(value: wasmtime_v128) : this(Kind.V128, value)
    constructor(value: CPointer<wasmtime_func_t>) : this(Kind.FUNCREF, value)
    constructor(value: ExternRef<*>) : this(Kind.EXTERNREF, value)

    constructor(valType: ValType<*>, value: Any) : this(Kind.fromValType(valType), value) {
        if (valType is ValType.I32 && value !is Int) {
            throw IllegalArgumentException("Expected Int for I32")
        } else if (valType is ValType.I64 && value !is Long) {
            throw IllegalArgumentException("Expected Long for I64")
        } else if (valType is ValType.F32 && value !is Float) {
            throw IllegalArgumentException("Expected Float for F32")
        } else if (valType is ValType.F64 && value !is Double) {
            throw IllegalArgumentException("Expected Double for F64")
        } else if (valType is ValType.FuncRef && value !is CPointer<*>) {
            throw IllegalArgumentException("Expected CPointer<wasmtime_func_t> for FuncRef")
        } else if (valType is ValType.AnyRef && value !is ExternRef<*>) {
            throw IllegalArgumentException("Expected ExternRef<*> for AnyRef")
        }
    }

    val i32: Int
        get() {
            require(kind == Kind.I32) { "Value is not i32" }
            return value as Int
        }

    val i64: Long
        get() {
            require(kind == Kind.I64) { "Value is not i64" }
            return value as Long
        }

    val f32: Float
        get() {
            require(kind == Kind.F32) { "Value is not f32" }
            return value as Float
        }

    val f64: Double
        get() {
            require(kind == Kind.F64) { "Value is not f64" }
            return value as Double
        }

    val v128: wasmtime_v128
        get() {
            require(kind == Kind.V128) { "Value is not v128" }
            return value as wasmtime_v128
        }

    val funcref: CPointer<wasmtime_func_t>
        get() {
            require(kind == Kind.FUNCREF) { "Value is not funcref" }
            return value as CPointer<wasmtime_func_t>
        }

    val externref: ExternRef<*>
        get() {
            require(kind == Kind.EXTERNREF) { "Value is not externref" }
            return value as ExternRef<*>
        }

    companion object {
        fun fromCValue(value: CPointer<wasmtime_val_t>): Val {
            return when (value.pointed.kind.toInt()) {
                WASMTIME_I32 -> Val(value.pointed.of.i32)
                WASMTIME_I64 -> Val(value.pointed.of.i64)
                WASMTIME_F32 -> Val(value.pointed.of.f32)
                WASMTIME_F64 -> Val(value.pointed.of.f64)
                WASMTIME_V128 -> Val(value.pointed.of.v128)
                WASMTIME_FUNCREF -> Val(value.pointed.of.funcref.ptr)
                WASMTIME_EXTERNREF -> Val(ExternRef<Any>(value.pointed.of.externref!!))
                else -> throw IllegalArgumentException("Invalid WasmtimeValKind value: ${value.pointed.kind}")
            }
        }

        fun kindFromCValue(wasmtimeVal: CPointer<wasmtime_val_t>): Kind {
            return Kind.fromValue(wasmtimeVal.pointed.kind.toInt())
        }

        fun deleteCValue(wasmtimeVal: CPointer<wasmtime_val_t>) {
            if (wasmtimeVal.pointed.kind.toInt() == WASMTIME_EXTERNREF) {
                wasmtime_externref_delete(wasmtimeVal.pointed.of.externref)
            }
            wasmtime_val_delete(wasmtimeVal)
        }

        fun allocateCValue(value: Val): CPointer<wasmtime_val_t> {
            return when (value.kind) {
                Val.Kind.I32 -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = WASMTIME_I32.toUByte()
                    of.i32 = value.value as Int
                }.ptr
                Val.Kind.I64 -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = WASMTIME_I64.toUByte()
                    of.i64 = value.value as Long
                }.ptr
                Val.Kind.F32 -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = WASMTIME_F32.toUByte()
                    of.f32 = value.value as Float
                }.ptr
                Val.Kind.F64 -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = WASMTIME_F64.toUByte()
                    of.f64 = value.value as Double
                }.ptr
                Val.Kind.V128 -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = WASMTIME_V128.toUByte()
                    memcpy(of.v128, value.value as wasmtime_v128, 16)
                }.ptr
                Val.Kind.FUNCREF -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = WASMTIME_FUNCREF.toUByte()
                    memcpy(of.funcref.ptr, value.value as CPointer<wasmtime_func_t>, sizeOf<wasmtime_func_t>().toULong())
                }.ptr
                Val.Kind.EXTERNREF -> nativeHeap.alloc<wasmtime_val_t>().apply {
                    kind = WASMTIME_EXTERNREF.toUByte()
                    of.externref = (value.value as ExternRef<*>).externRef
                }.ptr
            }
        }

        fun copyCVal(wasmtimeVal: CPointer<wasmtime_val_t>): CPointer<wasmtime_val_t> {
            val copy = nativeHeap.alloc<wasmtime_val_t>().ptr
            wasmtime_val_copy(copy, wasmtimeVal)
            return copy
        }
    }

    enum class Kind(val value: Int) {
        I32(0),
        I64(1),
        F32(2),
        F64(3),
        V128(4),
        FUNCREF(5),
        EXTERNREF(6);

        companion object {
            fun fromValue(value: Int): Kind {
                return values().find { it.value == value } ?: throw IllegalArgumentException("Invalid WasmtimeValKind value: $value")
            }

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
