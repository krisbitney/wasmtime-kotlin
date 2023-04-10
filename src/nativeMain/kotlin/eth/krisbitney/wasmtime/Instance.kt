package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import platform.posix.size_tVar
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class Instance(
    private val store: CPointer<wasmtime_context_t>,
    val instance: wasmtime_instance_t
) : AutoCloseable {

    constructor(
        store: Store<*>,
        module: Module,
        imports: List<Extern>,
    ) : this(
        store.context.context,
        memScoped {
            val cImports: CValuesRef<wasmtime_extern_t> = imports
                .map { it.extern }
                .toCValues()
                .ptr
                .reinterpret()

            val newInstance = nativeHeap.alloc<wasmtime_instance_t>()
            val trap = alloc<CPointerVar<wasm_trap_t>>()
            val error = wasmtime_instance_new(
                store.context.context,
                module.module,
                cImports,
                imports.size.convert(),
                newInstance.ptr,
                trap.ptr
            )

            if (error != null) {
                nativeHeap.free(newInstance)
                throw WasmtimeException(error)
            }

            if (trap.value != null) {
                nativeHeap.free(newInstance)
                throw Trap(trap.value!!)
            }

            newInstance
        }
    )

    fun getExport(name: String): Extern? {
        val wasmExtern = nativeHeap.alloc<wasmtime_extern_t>()
        val found = wasmtime_instance_export_get(
            store,
            instance.ptr,
            name,
            name.length.convert(),
            wasmExtern.ptr
        )

        return if (found) {
            Extern(store, wasmExtern.ptr)
        } else {
            nativeHeap.free(wasmExtern)
            null
        }
    }

    fun getExport(index: Int): Pair<String, Extern>? {
        val namePtr = nativeHeap.alloc<CPointerVar<ByteVar>>()
        val nameLen = nativeHeap.alloc<size_tVar>()
        val wasmExtern = nativeHeap.alloc<wasmtime_extern_t>()

        val found = wasmtime_instance_export_nth(
            store,
            instance.ptr,
            index.convert(),
            namePtr.ptr,
            nameLen.ptr,
            wasmExtern.ptr
        )

        return if (found) {
            val name = namePtr.value!!.toKString()
            nativeHeap.free(nameLen)
            nativeHeap.free(namePtr)
            val extern = Extern(store, wasmExtern.ptr)
            name to extern
        } else {
            nativeHeap.free(nameLen)
            nativeHeap.free(namePtr)
            nativeHeap.free(wasmExtern)
            null
        }
    }

    override fun close() {
        nativeHeap.free(instance)
    }
}
