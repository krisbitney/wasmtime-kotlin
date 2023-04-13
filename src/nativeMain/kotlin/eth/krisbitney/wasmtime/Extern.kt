package eth.krisbitney.wasmtime

import eth.krisbitney.wasmtime.wasm.ExternType
import kotlinx.cinterop.*
import wasmtime.wasmtime_context_t
import wasmtime.wasmtime_extern_delete
import wasmtime.wasmtime_extern_t
import wasmtime.wasmtime_extern_type

@OptIn(ExperimentalStdlibApi::class)
class Extern(
    private val store: CPointer<wasmtime_context_t>,
    val extern: CPointer<wasmtime_extern_t>
) : AutoCloseable {

    val kind: ExternType.Kind = ExternType.Kind.fromValue(extern.pointed.kind)

    val func: Func
        get() {
            require(kind == ExternType.Kind.FUNC) { "Extern is not a function" }
            return Func(store, extern.pointed.of.func.ptr)
        }

    val global: Global
        get() {
            require(kind == ExternType.Kind.GLOBAL) { "Extern is not a global" }
            return Global(store, extern.pointed.of.global.ptr)
        }

    val table: Table
        get() {
            require(kind == ExternType.Kind.TABLE) { "Extern is not a table" }
            return Table(store, extern.pointed.of.table.ptr)
        }

    val memory: Memory
        get() {
            require(kind == ExternType.Kind.MEMORY) { "Extern is not a memory" }
            return Memory(store, extern.pointed.of.memory.ptr)
        }

    fun type(): ExternType {
        val ptr = wasmtime_extern_type(store, extern) ?: throw Error("failed to get extern type")
        return ExternType.fromCValue(ptr)
    }

    override fun close() {
        wasmtime_extern_delete(extern)
    }
}