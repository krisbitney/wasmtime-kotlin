package eth.krisbitney.wasmtime

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

    @Test
    fun testCustomConfig() {
        Config().use { config ->
            config.setStrategy(Config.WasmtimeStrategy.CRANELIFT)
                .setCraneliftOptLevel(Config.OptLevel.SPEED_AND_SIZE)
                .setDebugInfo(true)
            Engine(config).use { engine ->
                Store(engine, false).use { store ->
                    store.engine
                }
            }
        }
    }
}
