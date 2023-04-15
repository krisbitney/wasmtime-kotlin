package eth.krisbitney.wasmtime

import kotlinx.cinterop.pointed
import kotlin.test.*

@OptIn(ExperimentalStdlibApi::class)
class ConfigTest {

    @Test
    fun testConfigConstructorAndClose() {
        val config = Config()
        assertNotNull(config)
        assertNotEquals(config.config.rawValue.toLong(), 0)
        assertNotNull(config.config.pointed)
        config.close()
    }

    @Test
    fun testSetDebugInfo() {
        val config = Config()
        config.setDebugInfo(true)
        config.close()
    }

    @Test
    fun testSetConsumeFuel() {
        val config = Config()
        config.setConsumeFuel(true)
        config.close()
    }

    @Test
    fun testSetEpochInterruption() {
        val config = Config()
        config.setEpochInterruption(true)
        config.close()
    }

    @Test
    fun testSetMaxWasmStack() {
        val config = Config()
        config.setMaxWasmStack(4096u)
        config.close()
    }

    @Test
    fun testSetWasmReferenceTypes() {
        val config = Config()
        config.setWasmReferenceTypes(true)
        config.setWasmReferenceTypes(false)
        config.close()
    }

    @Test
    fun testSetWasmSimd() {
        val config = Config()
        config.setWasmSimd(true)
        config.setWasmSimd(false)
        config.close()
    }

    @Test
    fun testSetWasmBulkMemory() {
        val config = Config()
        config.setWasmBulkMemory(true)
        config.setWasmBulkMemory(false)
        config.close()
    }

    @Test
    fun testSetWasmMultiValue() {
        val config = Config()
        config.setWasmMultiValue(true)
        config.setWasmMultiValue(false)
        config.close()
    }

    @Test
    fun testSetWasmMultiMemory() {
        val config = Config()
        config.setWasmMultiMemory(true)
        config.setWasmMultiMemory(false)
        config.close()
    }

    @Test
    fun testSetWasmMemory64() {
        val config = Config()
        config.setWasmMemory64(true)
        config.close()
    }

    @Test
    fun testSetStrategy() {
        val config = Config()
        config.setStrategy(Config.WasmtimeStrategy.CRANELIFT)
        config.setStrategy(Config.WasmtimeStrategy.AUTO)
        config.close()
    }

    @Test
    fun testSetParallelCompilation() {
        val config = Config()
        config.setParallelCompilation(true)
        config.close()
    }

    @Test
    fun testSetCraneliftDebugVerifier() {
        val config = Config()
        config.setCraneliftDebugVerifier(true)
        config.close()
    }

    @Test
    fun testSetCraneliftNanCanonicalization() {
        val config = Config()
        config.setCraneliftNanCanonicalization(true)
        config.close()
    }

    @Test
    fun testSetCraneliftOptLevel() {
        val config = Config()
        Config.OptLevel.values().forEach { optLevel ->
            config.setCraneliftOptLevel(optLevel)
        }
        config.close()
    }

    @Test
    fun testSetProfiler() {
        val config = Config()
        Config.ProfilingStrategy.values().forEach { profilingStrategy ->
            config.setProfiler(profilingStrategy)
        }
        config.close()
    }

    @Test
    fun testSetStaticMemoryMaximumSize() {
        val config = Config()
        config.setStaticMemoryMaximumSize(1024u)
        config.close()
    }

    @Test
    fun testSetStaticMemoryGuardSize() {
        val config = Config()
        config.setStaticMemoryGuardSize(4096u)
        config.close()
    }

    @Test
    fun testSetDynamicMemoryGuardSize() {
        val config = Config()
        config.setDynamicMemoryGuardSize(8192u)
        config.close()
    }

    @Test
    fun `test cacheConfigLoad with invalid path`() {
        val config = Config()
        val path = "path/to/invalid.toml"
        val result = config.cacheConfigLoad(path)
        assert(result.isFailure)
    }

    @Test
    fun `test cacheConfigLoad with null path`() {
        val config = Config()
        val result = config.cacheConfigLoad(null)
        assert(result.isSuccess)
    }

    @Ignore
    @Test
    fun testConfigInEngine() {
        val engine = Engine {
            setStrategy(Config.WasmtimeStrategy.CRANELIFT)
            setCraneliftOptLevel(Config.OptLevel.SPEED_AND_SIZE)
            setProfiler(Config.ProfilingStrategy.JITDUMP)
            setConsumeFuel(false)
            setMaxWasmStack(4096u)
            setDebugInfo(true)
        }
        val store = Store(engine, false)
        store.close()
        engine.close()
    }
}
