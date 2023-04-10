package io.polywrap.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class ExternType(val externType: CPointer<wasm_externtype_t>) : AutoCloseable {

    val kind: ExternKind = ExternKind.fromValue(wasm_externtype_kind(externType))

    fun toFuncType(): FuncType {
        require(kind == ExternKind.FUNC) { "Extern is not a function" }
        val funcTypePtr = wasm_externtype_as_functype(externType)!!
        return FuncType(funcTypePtr)
    }

    fun toGlobalType(): GlobalType {
        require(kind == ExternKind.GLOBAL) { "Extern is not a global" }
        val globalTypePtr = wasm_externtype_as_globaltype(externType)!!
        return GlobalType(globalTypePtr)
    }

    fun toTableType(): TableType {
        require(kind == ExternKind.TABLE) { "Extern is not a table" }
        val tableTypePtr = wasm_externtype_as_tabletype(externType)!!
        return TableType(tableTypePtr)
    }

    fun toMemoryType(): MemoryType {
        require(kind == ExternKind.MEMORY) { "Extern is not a memory" }
        val memoryTypePtr = wasm_externtype_as_memorytype(externType)!!
        return MemoryType(memoryTypePtr)
    }

    override fun close() {
        wasm_externtype_delete(externType)
    }

    companion object {
        fun fromWasmFuncType(funcType: FuncType): ExternType {
            val externTypePtr = wasm_functype_as_externtype(funcType.funcType)
                ?: throw Error("failed to get extern type from func type")
            return ExternType(externTypePtr)
        }

        fun fromWasmGlobalType(globalType: GlobalType): ExternType {
            val externTypePtr = wasm_globaltype_as_externtype(globalType.globalType)
                ?: throw Error("failed to get extern type from global type")
            return ExternType(externTypePtr)
        }

        fun fromWasmTableType(tableType: TableType): ExternType {
            val externTypePtr = wasm_tabletype_as_externtype(tableType.tableType)
                ?: throw Error("failed to get extern type from table type")
            return ExternType(externTypePtr)
        }

        fun fromWasmMemoryType(memoryType: MemoryType): ExternType {
            val externTypePtr = wasm_memorytype_as_externtype(memoryType.memoryType)
                ?: throw Error("failed to get extern type from memory type")
            return ExternType(externTypePtr)
        }
    }
}
