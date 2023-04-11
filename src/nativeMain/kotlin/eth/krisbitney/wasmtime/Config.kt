package eth.krisbitney.wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t
import wasmtime.*

@OptIn(ExperimentalStdlibApi::class)
class Config(val config: CPointer<wasm_config_t>) : AutoCloseable {

    constructor() : this(wasm_config_new() ?: throw RuntimeException("Failed to create wasmtime config"))

    fun setDebugInfo(value: Boolean): Config = this.apply {
        wasmtime_config_debug_info_set(config, value)
    }

    fun setConsumeFuel(value: Boolean): Config = this.apply {
        wasmtime_config_consume_fuel_set(config, value)

    }

    fun setEpochInterruption(value: Boolean): Config = this.apply {
        wasmtime_config_epoch_interruption_set(config, value)
    }

    fun setMaxWasmStack(size: size_t): Config = this.apply {
        wasmtime_config_max_wasm_stack_set(config, size)
    }

    fun setWasmThreads(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_threads_set(config, value)
    }

    fun setWasmReferenceTypes(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_reference_types_set(config, value)
    }

    fun setWasmSimd(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_simd_set(config, value)
    }

    fun setWasmBulkMemory(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_bulk_memory_set(config, value)
    }

    fun setWasmMultiValue(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_multi_value_set(config, value)
    }

    fun setWasmMultiMemory(value: Boolean) {
        wasmtime_config_wasm_multi_memory_set(config, value)
    }

    fun setWasmMemory64(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_memory64_set(config, value)
    }

    fun setStrategy(strategy: WasmtimeStrategy): Config = this.apply {
        wasmtime_config_strategy_set(config, strategy.ordinal.convert())
    }

    fun setParallelCompilation(value: Boolean): Config = this.apply {
        wasmtime_config_parallel_compilation_set(config, value)
    }

    fun setCraneliftDebugVerifier(value: Boolean): Config = this.apply {
        wasmtime_config_cranelift_debug_verifier_set(config, value)
    }

    fun setCraneliftNanCanonicalization(value: Boolean): Config = this.apply {
        wasmtime_config_cranelift_nan_canonicalization_set(config, value)
    }

    fun setCraneliftOptLevel(optLevel: OptLevel): Config = this.apply {
        wasmtime_config_cranelift_opt_level_set(config, optLevel.ordinal.convert())
    }

    fun setProfiler(profilingStrategy: ProfilingStrategy): Config = this.apply {
        wasmtime_config_profiler_set(config, profilingStrategy.ordinal.convert())
    }

    fun setStaticMemoryMaximumSize(size: ULong): Config = this.apply {
        wasmtime_config_static_memory_maximum_size_set(config, size)
    }

    fun setStaticMemoryGuardSize(size: ULong): Config = this.apply {
        wasmtime_config_static_memory_guard_size_set(config, size)
    }

    fun setDynamicMemoryGuardSize(size: ULong): Config = this.apply {
        wasmtime_config_dynamic_memory_guard_size_set(config, size)
    }

    fun cacheConfigLoad(path: String?): Result<Unit> {
        val error = wasmtime_config_cache_config_load(config, path)
        return if (error == null) {
            Result.success(Unit)
        } else {
            Result.failure(WasmtimeException(error))
        }
    }

    override fun close() {
        wasm_config_delete(config)
    }

    enum class WasmtimeStrategy {
        AUTO,
        CRANELIFT
    }

    enum class OptLevel {
        NONE,
        SPEED,
        SPEED_AND_SIZE
    }

    enum class ProfilingStrategy {
        NONE,
        JITDUMP,
        VTUNE
    }
}
