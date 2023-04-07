package wasm

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class WasmExternType(val externType: CPointer<wasm_externtype_t>) : AutoCloseable {

    val kind: WasmExternKind = WasmExternKind.fromValue(wasm_externtype_kind(externType))

    fun toFuncType(): WasmFuncType {
        require(kind == WasmExternKind.FUNC) { "Extern is not a function" }
        val funcTypePtr = wasm_externtype_as_functype(externType)!!
        return WasmFuncType(funcTypePtr)
    }

    fun toGlobalType(): WasmGlobalType {
        require(kind == WasmExternKind.GLOBAL) { "Extern is not a global" }
        val globalTypePtr = wasm_externtype_as_globaltype(externType)!!
        return WasmGlobalType(globalTypePtr)
    }

    fun toTableType(): WasmTableType {
        require(kind == WasmExternKind.TABLE) { "Extern is not a table" }
        val tableTypePtr = wasm_externtype_as_tabletype(externType)!!
        return WasmTableType(tableTypePtr)
    }

    fun toMemoryType(): WasmMemoryType {
        require(kind == WasmExternKind.MEMORY) { "Extern is not a memory" }
        val memoryTypePtr = wasm_externtype_as_memorytype(externType)!!
        return WasmMemoryType(memoryTypePtr)
    }

    override fun close() {
        wasm_externtype_delete(externType)
    }

    companion object {
        fun fromWasmFuncType(funcType: WasmFuncType): WasmExternType {
            val externTypePtr = wasm_functype_as_externtype(funcType.funcType)
                ?: throw Error("failed to get extern type from func type")
            return WasmExternType(externTypePtr)
        }

        fun fromWasmGlobalType(globalType: WasmGlobalType): WasmExternType {
            val externTypePtr = wasm_globaltype_as_externtype(globalType.globalType)
                ?: throw Error("failed to get extern type from global type")
            return WasmExternType(externTypePtr)
        }

        fun fromWasmTableType(tableType: WasmTableType): WasmExternType {
            val externTypePtr = wasm_tabletype_as_externtype(tableType.tableType)
                ?: throw Error("failed to get extern type from table type")
            return WasmExternType(externTypePtr)
        }

        fun fromWasmMemoryType(memoryType: WasmMemoryType): WasmExternType {
            val externTypePtr = wasm_memorytype_as_externtype(memoryType.memoryType)
                ?: throw Error("failed to get extern type from memory type")
            return WasmExternType(externTypePtr)
        }
    }
}
