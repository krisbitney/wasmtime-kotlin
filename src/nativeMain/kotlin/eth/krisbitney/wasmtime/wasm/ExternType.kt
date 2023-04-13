package eth.krisbitney.wasmtime.wasm

import kotlinx.cinterop.*
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class ExternType(val externType: CPointer<wasm_externtype_t>) : AutoCloseable {

    constructor(funcType: FuncType) : this(
        funcType.let {
            val externTypePtr = wasm_functype_as_externtype(it.funcType)
                ?: throw Error("failed to get extern type from func type")
            externTypePtr
        }
    )

    constructor(globalType: GlobalType) : this(
        globalType.let {
            val externTypePtr = wasm_globaltype_as_externtype(it.globalType)
                ?: throw Error("failed to get extern type from global type")
            externTypePtr
        }
    )

    constructor(tableType: TableType) : this(
        tableType.let {
            val externTypePtr = wasm_tabletype_as_externtype(it.tableType)
                ?: throw Error("failed to get extern type from table type")
            externTypePtr
        }
    )

    constructor(memoryType: MemoryType) : this(
        memoryType.let {
            val externTypePtr = wasm_memorytype_as_externtype(it.memoryType)
                ?: throw Error("failed to get extern type from memory type")
            externTypePtr
        }
    )

    val kind: Kind = Kind.fromValue(wasm_externtype_kind(externType))

    fun toFuncType(): FuncType {
        require(kind == Kind.FUNC) { "Extern is not a function" }
        val funcTypePtr = wasm_externtype_as_functype(externType)!!
        return FuncType(funcTypePtr)
    }

    fun toGlobalType(): GlobalType {
        require(kind == Kind.GLOBAL) { "Extern is not a global" }
        val globalTypePtr = wasm_externtype_as_globaltype(externType)!!
        return GlobalType(globalTypePtr)
    }

    fun toTableType(): TableType {
        require(kind == Kind.TABLE) { "Extern is not a table" }
        val tableTypePtr = wasm_externtype_as_tabletype(externType)!!
        return TableType(tableTypePtr)
    }

    fun toMemoryType(): MemoryType {
        require(kind == Kind.MEMORY) { "Extern is not a memory" }
        val memoryTypePtr = wasm_externtype_as_memorytype(externType)!!
        return MemoryType(memoryTypePtr)
    }

    override fun close() {
        wasm_externtype_delete(externType)
    }

    enum class Kind(val value: wasm_externkind_t) {
        FUNC(wasm_externkind_enum.WASM_EXTERN_FUNC.value.toUByte()),
        GLOBAL(wasm_externkind_enum.WASM_EXTERN_GLOBAL.value.toUByte()),
        TABLE(wasm_externkind_enum.WASM_EXTERN_TABLE.value.toUByte()),
        MEMORY(wasm_externkind_enum.WASM_EXTERN_MEMORY.value.toUByte());

        companion object {
            fun fromValue(value: wasm_externkind_t): Kind {
                return values().find { it.value == value }  ?: throw IllegalArgumentException("Invalid ExternKind value: $value")
            }
        }
    }
}
