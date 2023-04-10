package io.polywrap.wasmtime.wasm

import io.polywrap.wasmtime.ExternRef
import io.polywrap.wasmtime.Val
import kotlinx.cinterop.*
import wasmtime.*

sealed class ValType<T : Any>(val kind: Kind) {

    abstract fun fromVal(value: Val): T

    class I32() : ValType<Int>(Kind.I32) {
        override fun fromVal(value: Val): Int {
            return value.i32
        }
    }
    class I64() : ValType<Long>(Kind.I64) {
        override fun fromVal(value: Val): Long {
            return value.i64
        }
    }
    class F32() : ValType<Float>(Kind.F32) {
        override fun fromVal(value: Val): Float {
            return value.f32
        }
    }
    class F64() : ValType<Double>(Kind.F64) {
        override fun fromVal(value: Val): Double {
            return value.f64
        }
    }
    class AnyRef() : ValType<ExternRef<*>>(Kind.ANYREF) {
        override fun fromVal(value: Val): ExternRef<*> {
            return value.externref
        }
    }
    class FuncRef() : ValType<CPointer<wasmtime_func_t>>(Kind.FUNCREF) {
        override fun fromVal(value: Val): CPointer<wasmtime_func_t> {
            return value.funcref
        }
    }

    val isNum: Boolean
        get() = wasm_valkind_is_num(kind.value.toUByte())

    val isRef: Boolean
        get() = wasm_valkind_is_ref(kind.value.toUByte())

    companion object {
        fun fromKind(kind: Kind): ValType<*> {
            return when (kind) {
                Kind.I32 -> I32()
                Kind.I64 -> I64()
                Kind.F32 -> F32()
                Kind.F64 -> F64()
                Kind.ANYREF -> AnyRef()
                Kind.FUNCREF -> FuncRef()
            }
        }

        fun fromCValue(valType: CPointer<wasm_valtype_t>): ValType<*> {
            val kind = Kind.fromInt(wasm_valtype_kind(valType).toInt())
            return when (kind) {
                Kind.I32 -> I32()
                Kind.I64 -> I64()
                Kind.F32 -> F32()
                Kind.F64 -> F64()
                Kind.ANYREF -> AnyRef()
                Kind.FUNCREF -> FuncRef()
            }
        }

        fun kindFromCValue(valType: CPointer<wasm_valtype_t>): Kind {
            return Kind.fromInt(wasm_valtype_kind(valType).toInt())
        }

        fun deleteCValue(valType: CPointer<wasm_valtype_t>) {
            wasm_valtype_delete(valType)
        }

        fun allocateCValue(kind: Kind): CPointer<wasm_valtype_t> {
            return wasm_valtype_new(kind.value.toUByte()) ?: throw Error("failed to create wasm_valtype")
        }
    }

    enum class Kind(val value: Int) {
        I32(WASM_I32.toInt()),
        I64(WASM_I64.toInt()),
        F32(WASM_F32.toInt()),
        F64(WASM_F64.toInt()),
        ANYREF(WASM_ANYREF.toInt()),
        FUNCREF(WASM_FUNCREF.toInt());

        companion object {
            private val map = values().associateBy(Kind::value)
            fun fromInt(value: Int) = map[value] ?: error("Invalid WasmValKind value: $value")
        }
    }
}
