package eth.krisbitney.wasmtime

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertNotEquals

@OptIn(ExperimentalStdlibApi::class)
class ConfigTest {

    @Test
    fun testNewConfig() {
        Config().use { config ->
            assertNotEquals(config.config.rawValue.toLong(), 0)
        }
    }

    @Test
    fun testStrategy() {
        Config().use { config ->
            config.setStrategy(Config.WasmtimeStrategy.AUTO)
            config.setStrategy(Config.WasmtimeStrategy.CRANELIFT)
        }
    }

    @Test
    fun testOptLevel() {
        Config().use { config ->
            config
                .setStrategy(Config.WasmtimeStrategy.CRANELIFT)
                .setCraneliftOptLevel(Config.OptLevel.SPEED_AND_SIZE)
        }

        Config().use { config ->
            config
                .setStrategy(Config.WasmtimeStrategy.CRANELIFT)
                .setCraneliftOptLevel(Config.OptLevel.SPEED)
        }

        Config().use { config ->
            config
                .setStrategy(Config.WasmtimeStrategy.CRANELIFT)
                .setCraneliftOptLevel(Config.OptLevel.NONE)
        }

        Config().use { config ->
            config
                .setStrategy(Config.WasmtimeStrategy.AUTO)
                .setCraneliftOptLevel(Config.OptLevel.SPEED_AND_SIZE)
        }

        Config().use { config ->
            config
                .setStrategy(Config.WasmtimeStrategy.AUTO)
                .setCraneliftOptLevel(Config.OptLevel.SPEED)
        }

        Config().use { config ->
            config
                .setStrategy(Config.WasmtimeStrategy.AUTO)
                .setCraneliftOptLevel(Config.OptLevel.NONE)
        }
    }

    @Ignore
    @Test
    fun testCustomConfig() {
        val config = Config()
            .setStrategy(Config.WasmtimeStrategy.CRANELIFT)
            .setCraneliftOptLevel(Config.OptLevel.SPEED_AND_SIZE)
            .setDebugInfo(true)
        val engine = Engine(config)
        val store = Store(engine, false)
        store.close()
        engine.close()
    }
}
