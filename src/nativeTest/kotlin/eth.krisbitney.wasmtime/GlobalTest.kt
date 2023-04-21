package eth.krisbitney.wasmtime

import eth.krisbitney.wasmtime.wasm.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GlobalTests {

    private val engine = Engine()

    @Test
    fun testGlobalType() {
        val store = Store<Unit>(engine)
        val int32Type = GlobalType(ValType.Kind.I32, Mutability.CONST)
        val global = Global(store, int32Type, Val(42))

        val type = global.type
        assertEquals(int32Type, type, "GlobalType should be equal to the provided type")

        store.close()
    }

    @Test
    fun testGetInitialValue() {
        val store = Store<Unit>(engine)
        val int32Type = GlobalType(ValType.Kind.I32, Mutability.CONST)
        val initialValue =  Val(42)
        val global = Global(store, int32Type, initialValue)

        val value = global.get()
        assertEquals(initialValue, value, "Initial value should be equal to the provided value")

        store.close()
    }

    @Test
    fun testGetAndUpdateMutableGlobal() {
        val store = Store<Unit>(engine)
        val int32Type = GlobalType(ValType.Kind.I32, Mutability.VAR)
        val initialValue =  Val(42)
        val global = Global(store, int32Type, initialValue)

        val value1 = global.get()
        assertEquals(initialValue, value1, "Initial value should be equal to the provided value")

        val newValue = Val(24)
        global.set(newValue)

        val value2 = global.get()
        assertEquals(newValue, value2, "Updated value should be equal to the new value")

        store.close()
    }

    @Test
    fun testGetAndUpdateImmutableGlobal() {
        val store = Store<Unit>(engine)
        val int32Type = GlobalType(ValType.Kind.I32, Mutability.CONST)
        val initialValue =  Val(42)
        val global = Global(store, int32Type, initialValue)

        val newValue = Val(24)

        val exception = assertFailsWith<WasmtimeException> {
            global.set(newValue)
        }
        assertNotNull(exception, "Setting an immutable global should throw a WasmtimeException")

        store.close()
    }

    @Test
    fun testWrongValueType() {
        val store = Store<Unit>(engine)
        val int32Type = GlobalType(ValType.Kind.I32, Mutability.CONST)
        val wrongValue = Val(42.0f)

        val exception = assertFailsWith<WasmtimeException> {
            Global(store, int32Type, wrongValue)
        }
        assertNotNull(exception, "Creating a global with a wrong value type should throw a WasmtimeException")

        store.close()
    }
}
