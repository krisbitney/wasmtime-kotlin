package eth.krisbitney.wasmtime

import eth.krisbitney.wasmtime.util.toCValuesRef
import kotlinx.cinterop.*
import platform.posix.size_tVar
import wasmtime.*

/**
 * Represents an instance of a WebAssembly module.
 * An [Instance] provides access to the module's exported functions and values.
 * It is created by instantiating a compiled [Module] with the required [Extern] imports.
 *
 * @property instance The `wasmtime_instance_t` structure representing the WebAssembly instance.
 */
@OptIn(ExperimentalStdlibApi::class)
class Instance(
    private val store: CPointer<wasmtime_context_t>,
    val instance: CPointer<wasmtime_instance_t>,
) : AutoCloseable {

    /**
     * Instantiates the provided [Module] with the given [imports] in the [store].
     *
     * @param store The [Store] in which to create the instance.
     * @param module The compiled [Module] to instantiate.
     * @param imports The list of [Extern] imports to provide to the module during instantiation.
     * @throws [WasmtimeException] If there is an error during instantiation, such as a link error.
     * @throws Trap If a WebAssembly trap occurs during instantiation.
     */
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

            newInstance.ptr
        }
    )

    /**
     * Retrieves an exported value by its [name] from the instance.
     *
     * @param name The name of the export to retrieve.
     * @return The [Extern] representing the exported value, or `null` if the export with the given name is not found.
     */
    fun getExport(name: String): Extern? = memScoped {
        val wasmExtern = alloc<wasmtime_extern_t>()
        val found = wasmtime_instance_export_get(
            store,
            instance,
            name,
            name.length.convert(),
            wasmExtern.ptr
        )

        return if (found) {
            Extern.fromCValue(store, wasmExtern.ptr)
        } else {
            null
        }
    }

    /**
     * Retrieves an exported value by its [index] from the instance.
     *
     * @param index The index of the export to retrieve.
     * @return A [Pair] containing the name of the export and the [Extern] representing the exported value,
     * or `null` if the export with the given index is not found.
     */
    fun getExport(index: Int): Pair<String, Extern>? = memScoped {
        val namePtr = alloc<CPointerVar<ByteVar>>()
        val nameLen = alloc<size_tVar>()
        val wasmExtern = alloc<wasmtime_extern_t>()

        val found = wasmtime_instance_export_nth(
            store,
            instance,
            index.convert(),
            namePtr.ptr,
            nameLen.ptr,
            wasmExtern.ptr
        )

        return if (found) {
            val name = namePtr.value!!.toKString()
            val extern = Extern.fromCValue(store, wasmExtern.ptr)
            name to extern
        } else {
            null
        }
    }

    override fun close() {
        nativeHeap.free(instance)
    }
}
