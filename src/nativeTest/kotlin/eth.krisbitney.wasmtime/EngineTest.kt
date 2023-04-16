package eth.krisbitney.wasmtime

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class EngineTest {

    @Test
    fun `test create Engine with default configuration`() {
        val engine = Engine()
        assertNotNull(engine.engine)
        assertNotEquals(engine.engine.rawValue.toLong(), 0)
        engine.close()
    }

    @Test
    fun `test create Engine with custom configuration`() {
        val engine = Engine {
            setStaticMemoryMaximumSize(1024uL)
            setStaticMemoryGuardSize(4096uL)
            setDynamicMemoryGuardSize(8192uL)
            setStrategy(Config.WasmtimeStrategy.CRANELIFT)
            setCraneliftOptLevel(Config.OptLevel.SPEED_AND_SIZE)
            setMaxWasmStack(4096u)
            setDebugInfo(true)
        }
        assertNotNull(engine.engine)
        assertNotEquals(engine.engine.rawValue.toLong(), 0)
        engine.close()
    }

    @Test
    fun `test incrementEpoch`() {
        val engine = Engine()
        engine.incrementEpoch()
        engine.close()
    }

    @Test
    fun `test memory safety`() {
        val count = 10_000
        repeat(count) { Engine().close() }
    }
}
