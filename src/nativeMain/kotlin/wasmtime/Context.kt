package wasmtime

import kotlinx.cinterop.*

class Context<T>(val context: CPointer<wasmtime_context_t>) {
    inline fun <reified T>getData(): T? = wasmtime_context_get_data(context)?.asStableRef<Any>()?.get() as? T

    fun setData(data: T?) {
        val dataPtr: COpaquePointer? = data?.let { StableRef.create(it).asCPointer() }
        wasmtime_context_set_data(context, dataPtr)
        dataPtr?.asStableRef<Any>()?.dispose()
    }

    fun gc() {
        wasmtime_context_gc(context)
    }

    fun addFuel(fuel: ULong): Result<Unit> {
        val error = wasmtime_context_add_fuel(context, fuel)
        return if (error != null) {
            Result.failure(WasmtimeError(error))
        } else {
            Result.success(Unit)
        }
    }

    fun fuelConsumed(): ULong? = memScoped {
        val fuel = alloc<ULongVar>()
        val result = wasmtime_context_fuel_consumed(context, fuel.ptr)
        return if (result) fuel.value else null
    }

    fun consumeFuel(fuel: ULong): Result<ULong> = memScoped {
        val remaining = alloc<ULongVar>()
        val error = wasmtime_context_consume_fuel(context, fuel, remaining.ptr)
        return if (error == null) {
            Result.success(remaining.value)
        } else {
            Result.failure(WasmtimeError(error))
        }
    }

    fun setWasi(wasi: WasiConfig): Result<Unit> {
        val error = wasmtime_context_set_wasi(context, wasi.wasiConfig)
        return if (error == null) {
            Result.success(Unit)
        } else {
            Result.failure(WasmtimeError(error))
        }
    }

    fun setEpochDeadline(ticksBeyondCurrent: ULong) {
        wasmtime_context_set_epoch_deadline(context, ticksBeyondCurrent)
    }
}
