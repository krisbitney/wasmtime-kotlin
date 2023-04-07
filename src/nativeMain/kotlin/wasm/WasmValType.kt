package wasm

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class WasmValType(val valType: CPointer<wasm_valtype_t>) : AutoCloseable {

    enum class WasmValKind(val value: Int) {
        I32(WASM_I32.toInt()),
        I64(WASM_I64.toInt()),
        F32(WASM_F32.toInt()),
        F64(WASM_F64.toInt()),
        ANYREF(WASM_ANYREF.toInt()),
        FUNCREF(WASM_FUNCREF.toInt());

        companion object {
            private val map = values().associateBy(WasmValKind::value)
            fun fromInt(value: Int) = map[value] ?: error("Invalid WasmValKind value: $value")
        }
    }

    constructor(kind: WasmValKind) : this(wasm_valtype_new(kind.value.toUByte()) ?: throw Error("failed to create valtype"))

    val kind: WasmValKind
        get() = WasmValKind.fromInt(wasm_valtype_kind(valType).toInt())

    val isNum: Boolean
        get() = wasm_valkind_is_num(kind.value.toUByte())

    val isRef: Boolean
        get() = wasm_valkind_is_ref(kind.value.toUByte())

    override fun close() {
        wasm_valtype_delete(valType)
    }
}
