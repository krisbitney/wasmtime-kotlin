package eth.krisbitney.wasmtime

import eth.krisbitney.wasmtime.wasm.*
import kotlin.test.*

class WasmtimeExceptionTest {

    private lateinit var engine: Engine
    private lateinit var store: Store<Unit>

    @BeforeTest
    fun beforeEach() {
        engine = Engine()
        store = Store<Unit>(engine)
    }

    @AfterTest
    fun afterEach() {
        engine.close()
        store.close()
    }

    @Test
    fun textExceptionMemoryGrow() {
        val memoryType = MemoryType(Limits(1u, 2u))
        val memory = Memory(store, memoryType)

        val exception = runCatching {
            memory.grow(2u)
        }.exceptionOrNull() as? WasmtimeException?
        
        assertNotNull(exception)
        assertNotNull(exception.message)

        memory.close()
    }

    @Test
    fun textExceptionDeserializeModule() {
        val badBytes = byteArrayOf(0, 1, 2, 3, 4)
        val exception = runCatching {
            Module.deserialize(engine, badBytes)
        }.exceptionOrNull() as? WasmtimeException?

        assertNotNull(exception)
        assertNotNull(exception.message)
    }

    @Test
    fun textExceptionWrongGlobalType() {
        val int32Type = GlobalType(ValType.Kind.I32, Mutability.CONST)
        val wrongValue = Val(42.0f)
        val exception = runCatching {
            Global(store, int32Type, wrongValue)
        }.exceptionOrNull() as? WasmtimeException?

        assertNotNull(exception)
        assertNotNull(exception.message)
    }
}
