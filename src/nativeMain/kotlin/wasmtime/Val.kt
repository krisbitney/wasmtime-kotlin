package wasmtime

import kotlinx.cinterop.*
import platform.posix.memcpy

@OptIn(ExperimentalStdlibApi::class)
class Val(val wasmtimeVal: CPointer<wasmtime_val_t>) : AutoCloseable {

    constructor(value: Int) : this(
        nativeHeap.alloc<wasmtime_val_t>().apply {
            kind = WASMTIME_I32.toUByte()
            of.i32 = value
        }.ptr
    )

    constructor(value: Long) : this(
        nativeHeap.alloc<wasmtime_val_t>().apply {
            kind = WASMTIME_I64.toUByte()
            of.i64 = value
        }.ptr
    )

    constructor(value: Float) : this(
        nativeHeap.alloc<wasmtime_val_t>().apply {
            kind = WASMTIME_F32.toUByte()
            of.f32 = value
        }.ptr
    )

    constructor(value: Double) : this(
        nativeHeap.alloc<wasmtime_val_t>().apply {
            kind = WASMTIME_F64.toUByte()
            of.f64 = value
        }.ptr
    )

    constructor(value: wasmtime_v128) : this(
        nativeHeap.alloc<wasmtime_val_t>().apply {
            kind = WASMTIME_V128.toUByte()
            memcpy(of.v128, value, 16)
        }.ptr
    )

    constructor(value: Func) : this(
        nativeHeap.alloc<wasmtime_val_t>().apply {
            kind = WASMTIME_FUNCREF.toUByte()
            memcpy(of.funcref.ptr, value.func, sizeOf<wasmtime_func_t>().toULong())
        }.ptr
    )

    constructor(value: ExternRef<*>) : this(
        nativeHeap.alloc<wasmtime_val_t>().apply {
            kind = WASMTIME_EXTERNREF.toUByte()
            of.externref = value.externRef
        }.ptr
    )

    val kind: ValKind = ValKind.fromValue(wasmtimeVal.pointed.kind)

    val i32: Int
        get() {
            require(kind.value.toInt() == WASMTIME_I32) { "Value is not i32" }
            return wasmtimeVal.pointed.of.i32
        }

    val i64: Long
        get() {
            require(kind.value.toInt() == WASMTIME_I64) { "Value is not i64" }
            return wasmtimeVal.pointed.of.i64
        }

    val f32: Float
        get() {
            require(kind.value.toInt() == WASMTIME_F32) { "Value is not f32" }
            return wasmtimeVal.pointed.of.f32
        }

    val f64: Double
        get() {
            require(kind.value.toInt() == WASMTIME_F64) { "Value is not f64" }
            return wasmtimeVal.pointed.of.f64
        }

    val v128: wasmtime_v128
        get() {
            require(kind.value.toInt() == WASMTIME_V128) { "Value is not v128" }
            return wasmtimeVal.pointed.of.v128
        }

    val funcref:  wasmtime_func_t
        get() {
            require(kind.value.toInt() == WASMTIME_FUNCREF) { "Value is not funcref" }
            return wasmtimeVal.pointed.of.funcref
        }

    val externref: ExternRef<Any?>
        get() {
            require(kind.value.toInt() == WASMTIME_EXTERNREF) { "Value is not externref" }
            val ptr = wasmtimeVal.pointed.of.externref ?: throw IllegalArgumentException("ExternRef is null")
            return ExternRef(ptr)
        }

    fun copy(): Val {
        val copy = nativeHeap.alloc<wasmtime_val_t>().ptr
        wasmtime_val_copy(copy, wasmtimeVal)
        return Val(copy)
    }

    override fun close() {
        if (kind.value.toInt() == WASMTIME_EXTERNREF) {
            wasmtime_externref_delete(wasmtimeVal.pointed.of.externref)
        }
        wasmtime_val_delete(wasmtimeVal)
    }
}

enum class ValKind(val value: UByte) {
    I32(0u),
    I64(1u),
    F32(2u),
    F64(3u),
    V128(4u),
    FUNCREF(5u),
    EXTERNREF(6u);

    companion object {
        fun fromValue(value: UByte): ValKind {
            return values().find { it.value == value } ?: throw IllegalArgumentException("Invalid WasmtimeValKind value: $value")
        }
    }
}
