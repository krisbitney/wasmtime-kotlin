package eth.krisbitney.wasmtime

import kotlin.test.*

class StoreTest {

        @Test
        fun `test create Store with default data`() {
            val engine = Engine()
            val store = Store<Unit>(engine)
            assertNotNull(store)
            store.close()
            engine.close()
        }

        @Test
        fun `test create Store with custom data`() {
            val engine = Engine()
            val store = Store(engine, initData = "CustomData")
            assertNotNull(store)
            assertNotNull(store.data)
            store.close()
            engine.close()
        }

        @Test
        fun `test getContext`() {
            val engine = Engine()
            val store = Store(engine, initData = "CustomData")
            val context = store.context
            assertNotNull(context)
            assertNotNull(context.context)
            assertNotEquals(context.context.rawValue.toLong(), 0)
            store.close()
            engine.close()
        }

        @Test
        fun `test setLimiter`() {
            val engine = Engine()
            val store = Store(engine, initData = "CustomData")
            store.setLimiter(memorySize = 1024, tableElements = 128, instances = 20, tables = 20, memories = 20)
            store.close()
            engine.close()
        }
        @Test
        fun `test memory safety`() {
            val engine = Engine()
            val count = 10_000

            repeat(count) { i ->
                Store(engine, initData = i).close()
            }

            engine.close()
        }
}
