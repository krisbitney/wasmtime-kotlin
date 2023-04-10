package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.wasm_limits_t

data class WasmLimits(val min: UInt = 0u, val max: UInt = LIMITS_MAX_DEFAULT) {
    companion object {
        const val LIMITS_MAX_DEFAULT: UInt = 0xffffffffu

        fun cLimits(min: UInt = 0u, max: UInt = LIMITS_MAX_DEFAULT): wasm_limits_t {
            return cValue<wasm_limits_t> {
                this.min = min
                this.max = max
            }.useContents { this }
        }
    }
}
