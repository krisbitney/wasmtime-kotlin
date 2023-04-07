package wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t

@OptIn(ExperimentalStdlibApi::class)
class Config(val config: CPointer<wasm_config_t>) : AutoCloseable {

    constructor() : this(wasm_config_new() ?: throw RuntimeException("Failed to create wasmtime config"))

    fun setDebugInfo(value: Boolean) {
        wasmtime_config_debug_info_set(config, value)
    }

    fun setConsumeFuel(value: Boolean) {
        wasmtime_config_consume_fuel_set(config, value)
    }

    fun setEpochInterruption(value: Boolean) {
        wasmtime_config_epoch_interruption_set(config, value)
    }

    fun setMaxWasmStack(size: size_t) {
        wasmtime_config_max_wasm_stack_set(config, size)
    }

    fun setWasmThreads(value: Boolean) {
        wasmtime_config_wasm_threads_set(config, value)
    }

    fun setWasmReferenceTypes(value: Boolean) {
        wasmtime_config_wasm_reference_types_set(config, value)
    }

    fun setWasmSimd(value: Boolean) {
        wasmtime_config_wasm_simd_set(config, value)
    }

    fun setWasmBulkMemory(value: Boolean) {
        wasmtime_config_wasm_bulk_memory_set(config, value)
    }

    fun setWasmMultiValue(value: Boolean) {
        wasmtime_config_wasm_multi_value_set(config, value)
    }

    fun setWasmMultiMemory(value: Boolean) {
        wasmtime_config_wasm_multi_memory_set(config, value)
    }

    fun setWasmMemory64(value: Boolean) {
        wasmtime_config_wasm_memory64_set(config, value)
    }

    fun setStrategy(strategy: wasmtime_strategy_t) {
        wasmtime_config_strategy_set(config, strategy)
    }

    fun setParallelCompilation(value: Boolean) {
        wasmtime_config_parallel_compilation_set(config, value)
    }

    fun setCraneliftDebugVerifier(value: Boolean) {
        wasmtime_config_cranelift_debug_verifier_set(config, value)
    }

    fun setCraneliftNanCanonicalization(value: Boolean) {
        wasmtime_config_cranelift_nan_canonicalization_set(config, value)
    }

    fun setCraneliftOptLevel(optLevel: wasmtime_opt_level_t) {
        wasmtime_config_cranelift_opt_level_set(config, optLevel)
    }

    fun setProfiler(profilingStrategy: wasmtime_profiling_strategy_t) {
        wasmtime_config_profiler_set(config, profilingStrategy)
    }

    fun setStaticMemoryMaximumSize(size: ULong) {
        wasmtime_config_static_memory_maximum_size_set(config, size)
    }

    fun setStaticMemoryGuardSize(size: ULong) {
        wasmtime_config_static_memory_guard_size_set(config, size)
    }

    fun setDynamicMemoryGuardSize(size: ULong) {
        wasmtime_config_dynamic_memory_guard_size_set(config, size)
    }

    fun cacheConfigLoad(path: String?): Result<Unit> {
        val error = wasmtime_config_cache_config_load(config, path)
        return if (error == null) {
            Result.success(Unit)
        } else {
            Result.failure(WasmtimeError(error))
        }
    }

    override fun close() {
        wasm_config_delete(config)
    }
}
