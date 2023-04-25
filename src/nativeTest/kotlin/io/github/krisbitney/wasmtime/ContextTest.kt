package io.github.krisbitney.wasmtime

import kotlin.test.*

class ContextTest {
    private lateinit var engine: Engine
    private lateinit var store: Store<String>

    @BeforeTest
    fun beforeEach() {
        engine = Engine()
        store = Store(engine, "test-data")
    }

    @AfterTest
    fun afterEach() {
        store.close()
        engine.close()
    }

    @Test
    fun testGetData() {
        val context = store.context
        val data = context.getData<String>()
        assertEquals("test-data", data)
    }

    @Test
    fun testSetData() {
        val context = store.context
        context.setData("updated-data")
        val newData = context.getData<String>()
        assertEquals("updated-data", newData)
    }

    @Test
    fun testGc() {
        val context = store.context
        context.gc()
    }

    @Test
    fun testAddFuel() {
        val fuelEngine = Engine {
            setConsumeFuel(true)
        }
        val fuelStore = Store<Unit>(fuelEngine)
        val fuelContext = fuelStore.context

        val result = fuelContext.addFuel(100uL)
        assertTrue(result.isSuccess)

        fuelStore.close()
        fuelEngine.close()
    }

    @Test
    fun testConsumeFuel() {
        val fuelEngine = Engine {
            setConsumeFuel(true)
        }
        val fuelStore = Store<Unit>(fuelEngine)
        val fuelContext = fuelStore.context

        val addFuelResult = fuelContext.addFuel(100uL)
        assertTrue(addFuelResult.isSuccess)

        val result = fuelContext.consumeFuel(50uL)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFuelConsumed() {
        val fuelEngine = Engine {
            setConsumeFuel(true)
        }
        val fuelStore = Store<Unit>(fuelEngine)
        val fuelContext = fuelStore.context

        val addFuelResult = fuelContext.addFuel(100uL)
        assertTrue(addFuelResult.isSuccess)

        val consumeFuelResult = fuelContext.consumeFuel(50uL)
        assertTrue(consumeFuelResult.isSuccess)

        val fuel = fuelContext.fuelConsumed()
        assertNotNull(fuel)
        assertEquals(fuel, 50u)
    }

    @Test
    fun testSetWasi() {
        val context = store.context
        val result = context.setWasi {
            setArgv(listOf("test"))
            setEnv(mapOf("TEST" to "test"))
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSetEpochDeadline() {
        val context = store.context
        context.setEpochDeadline(1000uL)
    }
}