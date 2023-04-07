package wasmtime

import kotlinx.cinterop.*
import wasm.*

@OptIn(ExperimentalStdlibApi::class)
class Extern(
    private val store: CPointer<wasmtime_context_t>,
    val extern: CPointer<wasmtime_extern_t>
) : AutoCloseable {

    val kind: WasmExternKind = WasmExternKind.fromValue(extern.pointed.kind)

    val func: Func
        get() {
            require(kind == WasmExternKind.FUNC) { "Extern is not a function" }
            return Func(store, extern.pointed.of.func.ptr)
        }

    val global: Global
        get() {
            require(kind == WasmExternKind.GLOBAL) { "Extern is not a global" }
            return Global(store, extern.pointed.of.global.ptr)
        }

    val table: Table
        get() {
            require(kind == WasmExternKind.TABLE) { "Extern is not a table" }
            return Table(store, extern.pointed.of.table.ptr)
        }

    val memory: Memory
        get() {
            require(kind == WasmExternKind.MEMORY) { "Extern is not a memory" }
            return Memory(store, extern.pointed.of.memory.ptr)
        }

    fun type(): WasmExternType {
        val ptr = wasmtime_extern_type(store, extern) ?: throw Error("failed to get extern type")
        return WasmExternType(ptr)
    }

    override fun close() {
        type().close()
        wasmtime_extern_delete(extern)
    }
}