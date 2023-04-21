package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.wasm.ErrorFrame
import wasmtime.*

/**
 * Represents a WebAssembly trap, which is a runtime error that occurs during WebAssembly code execution.
 *
 * @property trap The C pointer to a [wasm_trap_t] struct.
 * @property message The trap message as a string.
 * @property trapCode The [TrapCode] enum value representing the type of trap that occurred, or `null` if the trap is not an instruction trap.
 * @property wasmTrace A list of [ErrorFrame] instances representing the WebAssembly stack trace.
 */
class Trap(private val trap: CPointer<wasm_trap_t>) : Throwable() {

    /**
     * Constructs a new [Trap] with the specified trap message.
     *
     * @param message The trap message.
     */
    constructor(message: String) : this(wasmtime_trap_new(message, message.length.convert()) ?: throw Exception("failed to create trap"))

    val trapCode: TrapCode? = memScoped {
        val code = alloc<wasmtime_trap_code_tVar>()
        if (wasmtime_trap_code(trap, code.ptr)) {
            TrapCode.fromValue(code.value.toUInt())
        } else {
            null
        }
    }

    val wasmTrace: List<ErrorFrame> = memScoped {
        val frameVec = alloc<wasm_frame_vec_t>()
        wasm_trap_trace(trap, frameVec.ptr)

        val frames = List(frameVec.size.toInt()) { index ->
            ErrorFrame(frameVec.data?.get(index)!!)
        }

        frames
    }

    override val message = memScoped {
        val msg = alloc<wasm_message_t>()
        wasm_trap_message(trap, msg.ptr)
        var result = msg.data?.toKString() ?: ""
        trapCode?.let { result += " (code: ${it.name})" }
        wasmTrace.forEach { frame -> result += "\n    $frame" }
        result
    }

    init {
        wasm_trap_delete(trap)
    }

    /**
     * Enumerates possible WebAssembly trap codes for instruction traps.
     *
     * @property value The numeric value representing the trap code.
     */
    enum class TrapCode(val value: UInt) {
        STACK_OVERFLOW(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_STACK_OVERFLOW.value),
        MEMORY_OUT_OF_BOUNDS(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_MEMORY_OUT_OF_BOUNDS.value),
        HEAP_MISALIGNED(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_HEAP_MISALIGNED.value),
        TABLE_OUT_OF_BOUNDS(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_TABLE_OUT_OF_BOUNDS.value),
        INDIRECT_CALL_TO_NULL(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_INDIRECT_CALL_TO_NULL.value),
        BAD_SIGNATURE(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_BAD_SIGNATURE.value),
        INTEGER_OVERFLOW(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_INTEGER_OVERFLOW.value),
        INTEGER_DIVISION_BY_ZERO(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_INTEGER_DIVISION_BY_ZERO.value),
        BAD_CONVERSION_TO_INTEGER(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_BAD_CONVERSION_TO_INTEGER.value),
        UNREACHABLE_CODE_REACHED(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_UNREACHABLE_CODE_REACHED.value),
        INTERRUPT(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_INTERRUPT.value),
        OUT_OF_FUEL(wasmtime_trap_code_enum.WASMTIME_TRAP_CODE_OUT_OF_FUEL.value);

        companion object {
            /**
             * Returns a [TrapCode] enum value corresponding to the given numeric trap code value.
             *
             * @param value The numeric trap code value.
             * @return The corresponding [TrapCode] enum value.
             * @throws IllegalArgumentException If the provided value does not correspond to any [TrapCode].
             */
            fun fromValue(value: UInt): TrapCode {
                return values().find { it.value == value } ?: throw IllegalArgumentException("Invalid TrapCode value: $value")
            }
        }
    }
}
