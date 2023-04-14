package eth.krisbitney.wasmtime

import eth.krisbitney.wasmtime.util.toCValuesRef
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
            val (cImportsRef, cImportsPtr) = imports.toCValuesRef()
            val newInstance = nativeHeap.alloc<wasmtime_instance_t>()
            val trap = alloc<CPointerVar<wasm_trap_t>>()
            val error = wasmtime_instance_new(
                store.context.context,
                module.module,
                cImportsRef,
                imports.size.convert(),
                newInstance.ptr,
                trap.ptr
            )
            nativeHeap.free(cImportsPtr)

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

        var extern: Extern? = null
        if (found) {
            extern = Extern.fromCValue(store, wasmExtern.ptr)
        }
        Extern.deleteCValue(wasmExtern.ptr)
        return extern
    }

    fun getExport(index: Int): Pair<String, Extern>? = memScoped {
        val namePtr = alloc<CPointerVar<ByteVar>>()
        val nameLen = alloc<size_tVar>()
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
            val extern = Extern.fromCValue(store, wasmExtern.ptr)
            Extern.deleteCValue(wasmExtern.ptr)
            name to extern
        } else {
            Extern.deleteCValue(wasmExtern.ptr)
            null
        }
    }

    override fun close() {
        nativeHeap.free(instance)
    }
}
