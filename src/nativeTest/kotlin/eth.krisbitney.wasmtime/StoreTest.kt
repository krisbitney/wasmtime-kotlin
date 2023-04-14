package eth.krisbitney.wasmtime

import kotlinx.cinterop.pointed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@OptIn(ExperimentalStdlibApi::class)
class StoreTest {

    @Test
    fun testCreate() {
        val store: Store<Unit> = Store(Engine(), Unit)
        store.close()
    }

    @Test
    fun testEngine() {
        Store(Engine(), Unit).use { store ->
            val engine: Engine = store.engine
            engine.close()
        }
    }

    @Test
    fun testStoreData() {
        val store: Store<Int> = Store(Engine(), 1234)
        assertEquals(1234, store.data)
        store.close()
    }
}
