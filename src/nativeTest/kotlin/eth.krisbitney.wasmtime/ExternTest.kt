package eth.krisbitney.wasmtime

import eth.krisbitney.wasmtime.wasm.*
import kotlin.test.*
import kotlinx.cinterop.*

class ExternTest {

    private lateinit var engine: Engine
    private lateinit var store: Store<Unit>
    private lateinit var memory: Memory
    private lateinit var table: Table
    private lateinit var func: Func
    private lateinit var global: Global

    @BeforeTest
    fun beforeEach() {
        engine = Engine()
        store = Store<Unit>(engine)

        // memory
        val memoryType = MemoryType(Limits(1u, 10u))
        memory = Memory(store, memoryType)

        // table
        val tableType = TableType(ValType.Kind.ANYREF, Limits(0u, 10u))
        val init = Val(ExternRef(null))
        table = Table(store, init, tableType)

        // func
        val paramTypes = listOf(ValType.I32(), ValType.I32())
        val resultTypes = listOf(ValType.I32())
        val funcType = FuncType(paramTypes.toTypedArray(), resultTypes.toTypedArray())

        val callback: FuncCallback = { _, args ->
            val x = args[0].i32
            val y = args[1].i32
            val sum = x + y
            Result.success(listOf(Val(sum)))
        }

        func = Func(store, funcType, callback)

        // global
        val int32Type = GlobalType(ValType.Kind.I32, Mutability.CONST)
        global = Global(store, int32Type, Val(42))
    }

    @AfterTest
    fun afterEach() {
        store.close()
        engine.close()
    }

    @Test
    fun testAllocateCValue() {
        Extern.Kind.values().forEach { kind ->
            val extern = when (kind) {
                Extern.Kind.FUNC -> func
                Extern.Kind.GLOBAL -> global
                Extern.Kind.TABLE -> table
                Extern.Kind.MEMORY -> memory
            }
            val cExtern = Extern.allocateCValue(extern)
            assertEquals(
                kind.value,
                cExtern.pointed.kind,
                "Expected kind: ${kind.value}, actual kind: ${cExtern.pointed.kind}"
            )
            when (kind) {
                Extern.Kind.FUNC -> {
                    assertEquals(
                        func.func.pointed.index,
                        cExtern.pointed.of.func.index,
                        "Expected func: ${func.func}, actual func: ${cExtern.pointed.of.func}"
                    )
                    assertEquals(
                        func.func.pointed.store_id,
                        cExtern.pointed.of.func.store_id,
                        "Expected func: ${func.func}, actual func: ${cExtern.pointed.of.func}"
                    )
                }
                Extern.Kind.GLOBAL -> {
                    assertEquals(
                        global.global.pointed.index,
                        cExtern.pointed.of.global.index,
                        "Expected global: ${global.global}, actual global: ${cExtern.pointed.of.global}"
                    )
                    assertEquals(
                        global.global.pointed.store_id,
                        cExtern.pointed.of.global.store_id,
                        "Expected global: ${global.global}, actual global: ${cExtern.pointed.of.global}"
                    )
                }
                Extern.Kind.TABLE -> {
                    assertEquals(
                        table.table.pointed.index,
                        cExtern.pointed.of.table.index,
                        "Expected table: ${table.table}, actual table: ${cExtern.pointed.of.table}"
                    )
                    assertEquals(
                        table.table.pointed.store_id,
                        cExtern.pointed.of.table.store_id,
                        "Expected table: ${table.table}, actual table: ${cExtern.pointed.of.table}"
                    )
                }
                Extern.Kind.MEMORY -> {
                    assertEquals(
                        memory.memory.pointed.index,
                        cExtern.pointed.of.memory.index,
                        "Expected memory: ${memory.memory}, actual memory: ${cExtern.pointed.of.memory}"
                    )
                    assertEquals(
                        memory.memory.pointed.store_id,
                        cExtern.pointed.of.memory.store_id,
                        "Expected memory: ${memory.memory}, actual memory: ${cExtern.pointed.of.memory}"
                    )
                }
            }
            Extern.deleteCValue(cExtern)
        }
    }

    @Test
    fun testDeleteCValue() {
        Extern.Kind.values().forEach { kind ->
            val extern = when (kind) {
                Extern.Kind.FUNC -> func
                Extern.Kind.GLOBAL -> global
                Extern.Kind.TABLE -> table
                Extern.Kind.MEMORY -> memory
            }
            val cExtern = Extern.allocateCValue(extern)
            Extern.deleteCValue(cExtern)
        }
    }

    @Test
    fun testFromCValue() {
        Extern.Kind.values().forEach { kind ->
            val extern = when (kind) {
                Extern.Kind.FUNC -> func
                Extern.Kind.GLOBAL -> global
                Extern.Kind.TABLE -> table
                Extern.Kind.MEMORY -> memory
            }
            val cExtern = Extern.allocateCValue(extern)
            val newExtern = Extern.fromCValue(store.context.context, cExtern)
            assertEquals(kind, newExtern.kind, "Expected kind: $kind, actual kind: ${extern.kind}")
            Extern.deleteCValue(cExtern)
        }
    }

    @Test
    fun testKindFromCValue() {
        Extern.Kind.values().forEach { kind ->
            val extern = when (kind) {
                Extern.Kind.FUNC -> func
                Extern.Kind.GLOBAL -> global
                Extern.Kind.TABLE -> table
                Extern.Kind.MEMORY -> memory
            }
            val cExtern = Extern.allocateCValue(extern)
            val actualKind = Extern.kindFromCValue(cExtern)
            assertEquals(kind, actualKind, "Expected kind: $kind, actual kind: $actualKind")
            Extern.deleteCValue(cExtern)
        }
    }

    @Test
    fun testKindFromValue() {
        // Test the Kind.fromValue method with valid input for each kind
        Extern.Kind.values().forEach { kind ->
            val actualKind = Extern.Kind.fromValue(kind.value)
            assertEquals(kind, actualKind, "Expected kind: $kind, actual kind: $actualKind")
        }
    }

    @Test
    fun testKindFromValueInvalid() {
        // Test the Kind.fromValue method with an invalid input value
        val invalidValue = 42.toUByte()
        assertFailsWith<IllegalArgumentException>(message = "Invalid Extern.Kind value: $invalidValue") {
            Extern.Kind.fromValue(invalidValue)
        }
    }
}

