package wasmtime

import kotlinx.cinterop.*
import wasm.WasmFrame

class Trap(private val trap: CPointer<wasm_trap_t>) : Throwable() {

    constructor(message: String) : this(wasmtime_trap_new(message, message.length.toULong()) ?: throw Error("failed to create trap"))

    override val message = memScoped {
        val msg = alloc<wasm_message_t>()
        wasm_trap_message(trap, msg.ptr)
        msg.data?.toKString()
    }

    val trapCode: TrapCode? = memScoped {
        val code = alloc<wasmtime_trap_code_tVar>()
        if (wasmtime_trap_code(trap, code.ptr)) {
            TrapCode.fromValue(code.value)
        } else {
            null
        }
    }

    init {
        wasm_trap_delete(trap)
    }

    companion object {
        fun frameFuncName(frame: WasmFrame): String? =
            wasmtime_frame_func_name(frame.frame)?.let { it.pointed.data?.toKString() }

        fun frameModuleName(frame: WasmFrame): String? =
            wasmtime_frame_module_name(frame.frame)?.let { it.pointed.data?.toKString() }
    }
}

enum class TrapCode(val value: UByte) {
    STACK_OVERFLOW(0u),
    MEMORY_OUT_OF_BOUNDS(1u),
    HEAP_MISALIGNED(2u),
    TABLE_OUT_OF_BOUNDS(3u),
    INDIRECT_CALL_TO_NULL(4u),
    BAD_SIGNATURE(5u),
    INTEGER_OVERFLOW(6u),
    INTEGER_DIVISION_BY_ZERO(7u),
    BAD_CONVERSION_TO_INTEGER(8u),
    UNREACHABLE_CODE_REACHED(9u),
    INTERRUPT(10u),
    OUT_OF_FUEL(11u);

    companion object {
        fun fromValue(value: UByte): TrapCode {
            return values().find { it.value == value } ?: throw IllegalArgumentException("Invalid TrapCode value: $value")
        }
    }
}
