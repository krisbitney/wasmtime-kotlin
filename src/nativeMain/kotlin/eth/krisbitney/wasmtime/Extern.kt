package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import platform.posix.memcpy
import wasmtime.*

sealed class Extern(
    val store: CPointer<wasmtime_context_t>,
    val kind: Kind,
) {

    companion object {
        /**
         * Constructs an [Extern] object from the given [extern] of type [wasmtime_extern_t].
         * The returned Extern is owned by the caller and must be deleted.
         * The argument extern: CPointer<wasmtime_extern_t> is also owned by the caller and must be deleted.
         *
         * @param store The [wasmtime_context_t] object representing the store.
         * @param extern The [wasmtime_extern_t] object representing the external value.
         * @return An [Extern] object corresponding to the given [extern].
         */
        fun fromCValue(store: CPointer<wasmtime_context_t>, extern: CPointer<wasmtime_extern_t>): Extern {
            val kind = Kind.fromValue(extern.pointed.kind)
            return when (kind) {
                Kind.FUNC -> {
                    val func = nativeHeap.alloc<wasmtime_func_t>()
                    memcpy(func.ptr, extern.pointed.of.func.ptr, sizeOf<wasmtime_func_t>().convert())
                    Func(store, func.ptr)
                }
                Kind.GLOBAL -> {
                    val global = nativeHeap.alloc<wasmtime_global_t>()
                    memcpy(global.ptr, extern.pointed.of.global.ptr, sizeOf<wasmtime_global_t>().convert())
                    Global(store, global.ptr)
                }
                Kind.TABLE -> {
                    val table = nativeHeap.alloc<wasmtime_table_t>()
                    memcpy(table.ptr, extern.pointed.of.table.ptr, sizeOf<wasmtime_table_t>().convert())
                    Table(store, table.ptr)
                }
                Kind.MEMORY -> {
                    val memory = nativeHeap.alloc<wasmtime_memory_t>()
                    memcpy(memory.ptr, extern.pointed.of.memory.ptr, sizeOf<wasmtime_memory_t>().convert())
                    Memory(store, memory.ptr)
                }
            }
        }

        fun kindFromCValue(extern: CPointer<wasmtime_extern_t>): Kind {
            return Kind.fromValue(extern.pointed.kind)
        }

        fun deleteCValue(extern: CPointer<wasmtime_extern_t>) {
            wasmtime_extern_delete(extern)
        }

        fun allocateCValue(extern: Extern): CPointer<wasmtime_extern_t> {
            val cExtern = nativeHeap.alloc<wasmtime_extern_t>()
            cExtern.kind = extern.kind.value
            when (extern) {
                is Func -> memcpy(cExtern.of.ptr, extern.func, sizeOf<wasmtime_extern_union_t>().convert())
                is Global -> memcpy(cExtern.of.ptr, extern.global, sizeOf<wasmtime_extern_union_t>().convert())
                is Table -> memcpy(cExtern.of.ptr, extern.table, sizeOf<wasmtime_extern_union_t>().convert())
                is Memory -> memcpy(cExtern.of.ptr, extern.memory, sizeOf<wasmtime_extern_union_t>().convert())
            }
            return cExtern.ptr
        }
    }

    /**
     * Represents the kind of external WebAssembly value type.
     *
     * @property value The integer value representing the [wasmtime_extern_kind_t] in the C API.
     */
    enum class Kind(val value: wasmtime_extern_kind_t) {
        FUNC(WASMTIME_EXTERN_FUNC.toUByte()),
        GLOBAL(WASMTIME_EXTERN_GLOBAL.toUByte()),
        TABLE(WASMTIME_EXTERN_TABLE.toUByte()),
        MEMORY(WASMTIME_EXTERN_MEMORY.toUByte());

        companion object {
            /**
             * Constructs a [Kind] object from the given [value] of type [wasmtime_extern_kind_t].
             *
             * @param value The [wasmtime_extern_kind_t] value representing the external value kind.
             * @return A [Kind] object corresponding to the given [value].
             * @throws IllegalArgumentException If the given [value] is not a valid [wasmtime_extern_kind_t].
             */
            fun fromValue(value: wasmtime_extern_kind_t): Kind {
                return values().find { it.value == value }  ?: throw IllegalArgumentException("Invalid Extern.Kind value: $value")
            }
        }
    }
}