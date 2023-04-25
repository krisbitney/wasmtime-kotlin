package io.github.krisbitney.wasmtime

import kotlinx.cinterop.*
import platform.posix.size_t
import wasmtime.*

/**
 * Represents a configuration for a Wasmtime instance. Wraps the native `wasm_config_t` structure.
 * This class provides methods for configuring various settings and features of Wasmtime.
 * This class should not be instantiated directly. Instead, use Config builder constructor in [Engine].
 *
 * @property config A pointer to the native `wasm_config_t` structure.
 * @constructor Creates a new instance of [Config] by allocating a new `wasm_config_t` structure.
 * @throws RuntimeException if the creation of a new `wasm_config_t` structure fails.
 */
@OptIn(ExperimentalStdlibApi::class)
class Config(val config: CPointer<wasm_config_t>) : AutoCloseable {

    constructor() : this(wasm_config_new() ?: throw RuntimeException("Failed to create wasm config"))

    /**
     * Sets whether DWARF debug information is constructed at runtime to describe JIT code.
     * By default, this setting is `false`. When enabled, it will attempt to inform native
     * debuggers about DWARF debugging information for JIT code to more easily debug compiled
     * WebAssembly via native debuggers. This can also sometimes improve the quality of output
     * when profiling is enabled.
     *
     * @param value A [Boolean] indicating whether to construct DWARF debug information at runtime.
     * @return A reference to this [Config] instance.
     */
    fun setDebugInfo(value: Boolean): Config = this.apply {
        wasmtime_config_debug_info_set(config, value)
    }


    /**
     * Sets the fuel consumption configuration for the Wasmtime engine.
     *
     * This method configures whether or not fuel is enabled for generated code. By default, this setting
     * is `false`. When enabled, it will enable fuel counting, meaning that fuel will be consumed every
     * time a Wasm instruction is executed and will trap when reaching zero. This is false by default.
     *
     * @param value A Boolean value representing whether to enable fuel consumption.
     * @return The Config object with the updated setting.
     */
    fun setConsumeFuel(value: Boolean): Config = this.apply {
        wasmtime_config_consume_fuel_set(config, value)

    }

    /**
     * Sets the epoch-based interruption configuration for the Wasmtime engine.
     *
     * This method configures whether or not epoch-based interruption is enabled for generated code. By
     * default, this setting is `false`. When enabled, Wasm code will check the current epoch
     * periodically and abort if the current epoch is beyond a store-configured limit.
     *
     * @param value A Boolean value representing whether to enable epoch-based interruption.
     * @return The Config object with the updated setting.
     */
    fun setEpochInterruption(value: Boolean): Config = this.apply {
        wasmtime_config_epoch_interruption_set(config, value)
    }

    /**
     * Sets the maximum stack size configuration for the Wasmtime engine.
     *
     * This method configures the maximum stack size, in bytes, that JIT code can use. By default, this
     * setting is 2MB. Configuring this setting will limit the amount of native stack space that JIT
     * code can use while it is executing.
     *
     * @param size A size_t value representing the maximum stack size, in bytes.
     * @return The Config object with the updated setting.
     */
    fun setMaxWasmStack(size: size_t): Config = this.apply {
        wasmtime_config_max_wasm_stack_set(config, size)
    }

    /**
     * Sets the WebAssembly threading proposal configuration for the Wasmtime engine.
     *
     * This method configures whether the WebAssembly threading proposal is enabled. By default, this
     * setting is `false`. Note that threads are largely unimplemented in Wasmtime at this time.
     *
     * @param value A Boolean value representing whether to enable WebAssembly threading.
     * @return The Config object with the updated setting.
     */
    fun setWasmThreads(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_threads_set(config, value)
    }

    /**
     * Sets the WebAssembly reference types proposal configuration for the Wasmtime engine.
     *
     * This method configures whether the WebAssembly reference types proposal is enabled. By default,
     * this setting is `false`.
     *
     * @param value A Boolean value representing whether to enable WebAssembly reference types.
     * @return The Config object with the updated setting.
     */
    fun setWasmReferenceTypes(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_reference_types_set(config, value)
    }

    /**
     * Enables or disables the WebAssembly SIMD proposal, which provides
     * a set of SIMD operations to perform computations in parallel.
     *
     * @param value A boolean value indicating whether to enable (true) or disable (false) the WebAssembly SIMD proposal.
     * @return Config Returns the updated Config instance.
     */
    fun setWasmSimd(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_simd_set(config, value)
    }

    /**
     * Enables or disables the WebAssembly bulk memory proposal, which extends
     * WebAssembly with new bulk memory operations for efficient memory manipulation.
     *
     * @param value A boolean value indicating whether to enable (true) or disable (false) the WebAssembly bulk memory proposal.
     * @return Config Returns the updated Config instance.
     */
    fun setWasmBulkMemory(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_bulk_memory_set(config, value)
    }

    /**
     * Enables or disables the WebAssembly multi value proposal, which allows functions
     * to return multiple values and adds new instructions to support multiple values.
     *
     * @param value A boolean value indicating whether to enable (true) or disable (false) the WebAssembly multi value proposal.
     * @return Config Returns the updated Config instance.
     */
    fun setWasmMultiValue(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_multi_value_set(config, value)
    }

    /**
     * Enables or disables the WebAssembly multi-memory proposal, which introduces
     * support for multiple linear memories within a single module.
     *
     * @param value A boolean value indicating whether to enable (true) or disable (false) the WebAssembly multi-memory proposal.
     */
    fun setWasmMultiMemory(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_multi_memory_set(config, value)
    }

    /**
     * Enables or disables the WebAssembly memory64 proposal, which allows linear
     * memory indices to be 64-bit, enabling applications to use more than 4 GB of memory.
     *
     * @param value A boolean value indicating whether to enable (true) or disable (false) the WebAssembly memory64 proposal.
     * @return Config Returns the updated Config instance.
     */
    fun setWasmMemory64(value: Boolean): Config = this.apply {
        wasmtime_config_wasm_memory64_set(config, value)
    }

    /**
     * Sets the compilation strategy for Wasmtime.
     *
     * This method configures how Wasmtime compiles WebAssembly code. Refer to the
     * `wasmtime_strategy_enum` in the C header file for available strategies.
     * The default strategy is `WASMTIME_STRATEGY_AUTO`, which automatically picks the
     * compilation backend (currently always defaulting to Cranelift).
     *
     * @param strategy The desired [WasmtimeStrategy] for Wasmtime to use.
     * @return This [Config] object, to allow for method chaining.
     */
    fun setStrategy(strategy: WasmtimeStrategy): Config = this.apply {
        wasmtime_config_strategy_set(config, strategy.value)
    }

    /**
     * Enables or disables parallel compilation for Wasmtime.
     *
     * This method configures whether Wasmtime should compile a module using multiple threads.
     *
     * @param value A [Boolean] indicating if parallel compilation should be enabled. `true` for enabled, `false` for disabled.
     * @return This [Config] object, to allow for method chaining.
     */
    fun setParallelCompilation(value: Boolean): Config = this.apply {
        wasmtime_config_parallel_compilation_set(config, value)
    }

    /**
     * Enables or disables Cranelift's debug verifier.
     *
     * This method configures whether Cranelift's debug verifier is enabled. When enabled,
     * it performs expensive debug checks within Cranelift itself to verify correctness.
     * By default, this setting is `false`.
     *
     * @param value A [Boolean] indicating if Cranelift's debug verifier should be enabled. `true` for enabled, `false` for disabled.
     * @return This [Config] object, to allow for method chaining.
     */
    fun setCraneliftDebugVerifier(value: Boolean): Config = this.apply {
        wasmtime_config_cranelift_debug_verifier_set(config, value)
    }

    /**
     * Enables or disables Cranelift's NaN canonicalization pass.
     *
     * This method configures whether Cranelift should perform a NaN-canonicalization pass
     * to replace NaNs with a single canonical value. This is useful for users requiring
     * entirely deterministic WebAssembly computation. This is not required by the WebAssembly
     * spec, so it is not enabled by default. The default value is `false`.
     *
     * @param value A [Boolean] indicating if Cranelift's NaN canonicalization should be enabled. `true` for enabled, `false` for disabled.
     * @return This [Config] object, to allow for method chaining.
     */
    fun setCraneliftNanCanonicalization(value: Boolean): Config = this.apply {
        wasmtime_config_cranelift_nan_canonicalization_set(config, value)
    }

    /**
     * Sets Cranelift's optimization level for JIT code.
     *
     * This method configures the optimization level used by Cranelift for JIT code generation.
     * Refer to the `wasmtime_opt_level_enum` in the C header file for available optimization levels.
     * The default optimization level is `WASMTIME_OPT_LEVEL_SPEED`, which optimizes purely for speed.
     *
     * @param optLevel The desired [OptLevel] for Cranelift's optimization.
     * @return This [Config] object, to allow for method chaining.
     */
    fun setCraneliftOptLevel(optLevel: OptLevel): Config = this.apply {
        wasmtime_config_cranelift_opt_level_set(config, optLevel.value)
    }

    /**
     * Sets the profiling strategy for the Wasmtime configuration.
     *
     * @param profilingStrategy The [ProfilingStrategy] to be used.
     * @return A [Config] object with the applied profiling strategy.
     * @see wasmtime_profiling_strategy_enum for possible values.
     */
    fun setProfiler(profilingStrategy: ProfilingStrategy): Config = this.apply {
        wasmtime_config_profiler_set(config, profilingStrategy.value)
    }

    /**
     * Sets the maximum size for memory to be considered "static" in the Wasmtime configuration.
     *
     * @param size The maximum size of the static memory in bytes.
     * @return A [Config] object with the applied static memory maximum size.
     */
    fun setStaticMemoryMaximumSize(size: ULong): Config = this.apply {
        wasmtime_config_static_memory_maximum_size_set(config, size)
    }

    /**
     * Sets the guard region size for "static" memory in the Wasmtime configuration.
     *
     * @param size The size of the guard region for static memory in bytes.
     * @return A [Config] object with the applied static memory guard size.
     */
    fun setStaticMemoryGuardSize(size: ULong): Config = this.apply {
        wasmtime_config_static_memory_guard_size_set(config, size)
    }

    /**
     * Sets the guard region size for "dynamic" memory in the Wasmtime configuration.
     *
     * @param size The size of the guard region for dynamic memory in bytes.
     * @return A [Config] object with the applied dynamic memory guard size.
     */
    fun setDynamicMemoryGuardSize(size: ULong): Config = this.apply {
        wasmtime_config_dynamic_memory_guard_size_set(config, size)
    }

    /**
     * Enables Wasmtime's cache and loads the configuration from the specified path.
     *
     * @param path The path to the TOML configuration file or null to use the default settings.
     * @return A [Result] object containing [Unit] if successful or a [WasmtimeException] if an error occurred.
     * @see wasmtime_config_cache_config_load
     */
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

    /**
     * Represents the different strategies Wasmtime can use to compile WebAssembly code.
     *
     * @property AUTO Automatically picks the compilation backend, currently always defaulting to Cranelift.
     * @property CRANELIFT Unconditionally uses Cranelift to compile WebAssembly code.
     */
    enum class WasmtimeStrategy(val value: wasmtime_strategy_t) {
        AUTO(wasmtime_strategy_enum.WASMTIME_STRATEGY_AUTO.value.toUByte()),
        CRANELIFT(wasmtime_strategy_enum.WASMTIME_STRATEGY_CRANELIFT.value.toUByte());
    }

    /**
     * Represents the optimization levels for generated JIT code in Wasmtime.
     *
     * @property NONE Generated code will not be optimized at all.
     * @property SPEED Generated code will be optimized purely for speed.
     * @property SPEED_AND_SIZE Generated code will be optimized, but some speed optimizations are
     *                           disabled if they cause the generated code to be significantly larger.
     */
    enum class OptLevel(val value: wasmtime_opt_level_t) {
        NONE(wasmtime_opt_level_enum.WASMTIME_OPT_LEVEL_NONE.value.toUByte()),
        SPEED(wasmtime_opt_level_enum.WASMTIME_OPT_LEVEL_SPEED.value.toUByte()),
        SPEED_AND_SIZE(wasmtime_opt_level_enum.WASMTIME_OPT_LEVEL_SPEED_AND_SIZE.value.toUByte());
    }

    /**
     * Represents the different profiling strategies for JIT code in Wasmtime.
     *
     * @property NONE No profiling is enabled at runtime.
     * @property JITDUMP Linux's "jitdump" support in `perf` is enabled and when Wasmtime is run
     *                    under `perf`, necessary calls will be made to profile generated JIT code.
     * @property VTUNE Support for VTune will be enabled, and the VTune runtime will be informed,
     *                  at runtime, about JIT code. Note that this isn't always enabled at build time.
     */
    enum class ProfilingStrategy(val value: wasmtime_profiling_strategy_t) {
        NONE(wasmtime_profiling_strategy_enum.WASMTIME_PROFILING_STRATEGY_NONE.value.toUByte()),
        JITDUMP(wasmtime_profiling_strategy_enum.WASMTIME_PROFILING_STRATEGY_JITDUMP.value.toUByte()),
        VTUNE(wasmtime_profiling_strategy_enum.WASMTIME_PROFILING_STRATEGY_VTUNE.value.toUByte()),
        PERFMAP(wasmtime_profiling_strategy_enum.WASMTIME_PROFILING_STRATEGY_PERFMAP.value.toUByte());
    }
}
