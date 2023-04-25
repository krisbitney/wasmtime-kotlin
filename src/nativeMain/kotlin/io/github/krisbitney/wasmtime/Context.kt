package io.github.krisbitney.wasmtime

import kotlinx.cinterop.*
import wasmtime.*

/**
 * Represents a Wasmtime context for a [Store], providing access to various Wasmtime operations
 * such as interacting with user data, managing fuel, and configuring WASI.
 * The context is an interior pointer to a [Store] has the same lifetime as the [Store].
 * A [Context] instance should not be used after its associated [Store] is closed or destroyed.
 *
 * @param T The type of data to be associated with the [Context].
 * @property context The native [wasmtime_context_t] pointer.
 */
class Context<T>(val context: CPointer<wasmtime_context_t>) {
    /**
     * Retrieves the user-specified data associated with the [Store] this [Context] belongs to.
     * @return The associated data, or `null` if no data is set.
     */
    inline fun <reified T>getData(): T? = wasmtime_context_get_data(context)?.asStableRef<Any>()?.get() as? T


    /**
     * Sets or updates the user-specified data associated with the [Store] this [Context] belongs to.
     * @param data The data to be set, or `null` to clear the data.
     */
    fun setData(data: T?) {
        val oldDataPtr = wasmtime_context_get_data(context)
        val dataPtr: COpaquePointer? = data?.let { StableRef.create(it).asCPointer() }
        wasmtime_context_set_data(context, dataPtr)
        oldDataPtr?.asStableRef<Any>()?.dispose()
    }

    /**
     * Performs garbage collection within the given [Context], running finalizers for unreachable `externref`s.
     */
    fun gc() {
        wasmtime_context_gc(context)
    }

    /**
     * Adds fuel to the [Store] for WebAssembly to consume while executing.
     * This function requires fuel consumption to be enabled in the [Config].
     * By default, a [Store] starts with 0 fuel, causing WebAssembly to immediately trap.
     * This function should be called to provide fuel to allow WebAssembly execution.
     *
     * @param fuel The amount of fuel to add to the [Store].
     * @return [Result.success] if fuel is successfully added, or [Result.failure] with a [WasmtimeException] if an error occurs.
     */
    fun addFuel(fuel: ULong): Result<Unit> {
        val error = wasmtime_context_add_fuel(context, fuel)
        return if (error != null) {
            Result.failure(WasmtimeException(error))
        } else {
            Result.success(Unit)
        }
    }

    /**
     * Returns the amount of fuel consumed by the [Store] so far.
     * @return The amount of fuel consumed, or `null` if fuel consumption is not enabled.
     */
    fun fuelConsumed(): ULong? = memScoped {
        val fuel = alloc<ULongVar>()
        val result = wasmtime_context_fuel_consumed(context, fuel.ptr)
        return if (result) fuel.value else null
    }

    /**
     * Attempts to manually consume fuel from the [Store].
     * @param fuel The amount of fuel to consume.
     * @return [Result.success] with the remaining amount of fuel if successful, or [Result.failure] with a [WasmtimeException] if an error occurs.
     */
    fun consumeFuel(fuel: ULong): Result<ULong> = memScoped {
        val remaining = alloc<ULongVar>()
        val error = wasmtime_context_consume_fuel(context, fuel, remaining.ptr)
        return if (error == null) {
            Result.success(remaining.value)
        } else {
            Result.failure(WasmtimeException(error))
        }
    }

    /**
     * Configures the WASI state within the [Store] this [Context] belongs to using the given [configure] function.
     * This function is required if defineWasi is called in the [Linker].
     * The WASI state is configured for instances defined within this [Store].
     *
     * @param configure A lambda that configures the WASI state using the [WasiConfig] receiver.
     * @return [Result.success] if the WASI state is successfully configured, or [Result.failure] with a [WasmtimeException] if an error occurs.
     */
    fun setWasi(configure: WasiConfig.() -> Unit): Result<Unit> {
        val config = WasiConfig().apply(configure)
        val error = wasmtime_context_set_wasi(context, config.wasiConfig)
        return if (error == null) {
            Result.success(Unit)
        } else {
            Result.failure(WasmtimeException(error))
        }
    }

    /**
     * Sets the relative deadline (in ticks beyond the current epoch) at which point WebAssembly code will trap.
     * This function configures the store-local epoch deadline after which point WebAssembly code will trap.
     *
     * @param ticksBeyondCurrent The number of ticks beyond the current epoch to set as the deadline.
     */
    fun setEpochDeadline(ticksBeyondCurrent: ULong) {
        wasmtime_context_set_epoch_deadline(context, ticksBeyondCurrent)
    }
}
