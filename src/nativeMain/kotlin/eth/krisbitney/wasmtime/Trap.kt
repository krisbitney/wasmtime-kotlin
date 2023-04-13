package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.wasm.WasmFrame
import wasmtime.*

/**
 * Represents a WebAssembly trap, which is a runtime error that occurs during WebAssembly code execution.
 *
 * @property trap The C pointer to a [wasm_trap_t] struct.
 * @property message The trap message as a string.
 * @property trapCode The [TrapCode] enum value representing the type of trap that occurred, or `null` if the trap is not an instruction trap.
 * @property wasmTrace A list of [WasmFrame] instances representing the WebAssembly stack trace.
 *
 * @constructor Constructs a new [Trap] from the given [wasm_trap_t] pointer.
 * @param trap The C pointer to a [wasm_trap_t] struct.
 *
 * @constructor Constructs a new [Trap] with the specified trap message.
 * @param message The trap message.
 */
class Trap(private val trap: CPointer<wasm_trap_t>) : Throwable() {

    constructor(message: String) : this(wasmtime_trap_new(message, message.length.convert()) ?: throw Error("failed to create trap"))

    override val message = memScoped {
        val msg = alloc<wasm_message_t>()
        wasm_trap_message(trap, msg.ptr)
        msg.data?.toKString() + " (code: ${trapCode?.name})"
    }

    val trapCode: TrapCode? = memScoped {
        val code = alloc<wasmtime_trap_code_tVar>()
        if (wasmtime_trap_code(trap, code.ptr)) {
            TrapCode.fromValue(code.value)
        } else {
            null
        }
    }

    val wasmTrace: List<WasmFrame> = memScoped {
        val frameVec = alloc<wasm_frame_vec_t>()
        wasm_trap_trace(trap, frameVec.ptr)

        val frames = List(frameVec.size.toInt()) { index ->
            WasmFrame(frameVec.data?.get(index)!!)
        }

        wasm_frame_vec_delete(frameVec.ptr)

        frames
    }

    init {
        wasm_trap_delete(trap)
    }

    /**
     * Enumerates possible WebAssembly trap codes for instruction traps.
     *
     * @property value The numeric value representing the trap code.
     */
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
            /**
             * Returns a [TrapCode] enum value corresponding to the given numeric trap code value.
             *
             * @param value The numeric trap code value.
             * @return The corresponding [TrapCode] enum value.
             * @throws IllegalArgumentException If the provided value does not correspond to any [TrapCode].
             */
            fun fromValue(value: UByte): TrapCode {
                return values().find { it.value == value } ?: throw IllegalArgumentException("Invalid TrapCode value: $value")
            }
        }
    }
}
