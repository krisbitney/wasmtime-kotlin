package eth.krisbitney.wasmtime

import kotlin.test.*
import eth.krisbitney.wasmtime.wasm.*

class TableTest {

    private lateinit var engine: Engine
    private lateinit var store: Store<Unit>

    @BeforeTest
    fun beforeEach() {
        engine = Engine()
        store = Store<Unit>(engine)
    }

    @AfterTest
    fun afterEach() {
        store.close()
        engine.close()
    }

    @Test
    fun testTableCreation() {
        val tableType = TableType(ValType.Kind.ANYREF, Limits(0u, 10u))
        val init = Val(ExternRef(null))

        val table = Table(store, init, tableType)
        assertNotNull(table, "Table should be created successfully")
        assertEquals(tableType, table.type, "Table type should match the input table type")
        assertEquals(0u, table.size, "Table size should be 0")
    }

    @Test
    fun testTableGetUninitialized() {
        val tableType = TableType(ValType.Kind.ANYREF, Limits(0u, 10u))
        val externRef = ExternRef(null)
        val init = Val(externRef)

        val table = Table(store, init, tableType)
        val value = table.get(0u)
        assertNull(value, "Table should return null for uninitialized value")
    }

    @Test
    fun testTableGrow() {
        val tableType = TableType(ValType.Kind.ANYREF, Limits(0u, 10u))
        val init = Val(ExternRef(null))
        val table = Table(store, init, tableType)

        assertEquals(0u, table.size, "Initial table size should be 0")

        val prevSize = table.grow(5u, Val(ExternRef(42)))
        assertEquals(0u, prevSize, "Previous table size should be 0")
        assertEquals(5u, table.size, "Table size after growth should be 5")

        assertEquals(42, table.get(0u)?.externref?.data<Int>(), "Table should return the set value")
        assertEquals(42, table.get(4u)?.externref?.data<Int>(), "Table should return the set value")
        assertEquals(null, table.get(6u), "Table should return null for uninitialized value")
    }

    @Test
    fun testTableGrowExceedingMaxSize() {
        val tableType = TableType(ValType.Kind.ANYREF, Limits(0u, 10u))
        val init = Val(ExternRef(null))
        val table = Table(store, init, tableType)

        assertFailsWith<WasmtimeException> {
            table.grow(11u, Val(ExternRef(42)))
        }
    }

    @Test
    fun testTableSetAndGet() {
        val tableType = TableType(ValType.Kind.ANYREF, Limits(0u, 10u))
        val init = Val(ExternRef(null))
        val table = Table(store, init, tableType)

        table.grow(5u, Val(ExternRef(null)))

        val value = Val(ExternRef(42))
        table.set(0u, value)
        val result = table.get(0u)
        assertEquals(42, result?.externref?.data<Int>(), "Table should return the set value")
    }

    @Test
    fun testTableSetOutOfBounds() {
        val tableType = TableType(ValType.Kind.ANYREF, Limits(0u, 10u))
        val init = Val(ExternRef(null))
        val table = Table(store, init, tableType)

        assertFailsWith<WasmtimeException> {
            table.set(11u, Val(ExternRef(16)))
        }
    }
}
