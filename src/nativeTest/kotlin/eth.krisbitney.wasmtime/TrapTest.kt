package eth.krisbitney.wasmtime

import util.divideByZeroTrap
import util.memoryOutOfBoundsTrap
import util.unreachableTrap
import kotlin.test.*

class TrapTest {

    @Test
    fun testUnreachableTrap() {
        val trap = createTrap(unreachableTrap)
        assertNotNull(trap)
        assertNotNull(trap.message)
        assertNotNull(trap.trapCode)
        assertEquals(trap.trapCode, Trap.TrapCode.UNREACHABLE_CODE_REACHED)
        assertFalse(trap.wasmTrace.isEmpty())
    }

    @Test
    fun testDivideByZeroTrap() {
        val trap = createTrap(divideByZeroTrap)
        assertNotNull(trap)
        assertNotNull(trap.message)
        assertNotNull(trap.trapCode)
        assertEquals(trap.trapCode, Trap.TrapCode.INTEGER_DIVISION_BY_ZERO)
        assertFalse(trap.wasmTrace.isEmpty())
    }

    @Test
    fun testMemoryOutOfBoundsTrap() {
        val trap = createTrap(memoryOutOfBoundsTrap)
        assertNotNull(trap)
        assertNotNull(trap.message)
        assertNotNull(trap.trapCode)
        assertEquals(trap.trapCode, Trap.TrapCode.MEMORY_OUT_OF_BOUNDS)
        assertFalse(trap.wasmTrace.isEmpty())
    }

    private fun createTrap(wat: String): Trap? {
        val engine = Engine()
        val module = Module(engine, wat)
        val store = Store<Unit>(engine)
        val linker = Linker(engine).module(store, "mod", module)

        val extern = linker.get(store, "mod", "traps")
        assertNotNull(extern)
        assertTrue(extern is Func)

        val trap = runCatching { extern.call(listOf(Val(1))) }

        module.close()
        linker.close()
        store.close()
        engine.close()

        return trap.exceptionOrNull() as? Trap?
    }
}