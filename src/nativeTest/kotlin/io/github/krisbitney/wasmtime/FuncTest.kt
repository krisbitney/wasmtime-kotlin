package io.github.krisbitney.wasmtime

import io.github.krisbitney.wasmtime.wasm.*
import kotlin.test.*

class FuncTest {

    private val engine = Engine()

    @Test
    fun testFuncCall() {
        val store = Store<Unit>(engine)
        val paramTypes = listOf(ValType.I32(), ValType.I64())
        val resultTypes = listOf(ValType.I32())
        val funcType = FuncType(paramTypes.toTypedArray(), resultTypes.toTypedArray())

        val callback: FuncCallback = { _, args ->
            val x = args[0].i32
            val y = args[1].i64
            val sum = x + y.toInt()
            Result.success(listOf(Val(sum)))
        }

        val func = Func(store, funcType, callback)
        val args = listOf(Val(1), Val(2L))
        val results = func.call(args)

        assertEquals(1, results.size)
        assertEquals(Val(3), results[0])
        
        store.close()
    }

    @Test
    fun testFuncCallWithDifferentNumberOfArgs() {
        val store = Store<Unit>(engine)
        val paramTypes = listOf(ValType.I32())
        val resultTypes = listOf(ValType.I32())
        val funcType = FuncType(paramTypes.toTypedArray(), resultTypes.toTypedArray())

        val callback: FuncCallback = { _, args ->
            val x = args[0].i32
            Result.success(listOf(Val(x + 1)))
        }

        val func = Func(store, funcType, callback)

        assertFailsWith<IllegalArgumentException> {
            func.call(listOf(Val(1), Val(2L)))
        }

        store.close()
    }

    @Test
    fun testFuncCallWithInvalidArgType() {
        val store = Store<Unit>(engine)
        val paramTypes = listOf(ValType.I32(), ValType.I64())
        val resultTypes = listOf(ValType.I32())
        val funcType = FuncType(paramTypes.toTypedArray(), resultTypes.toTypedArray())

        val callback: FuncCallback = { _, args ->
            val x = args[0].i32
            val y = args[1].i64
            val sum = x + y.toInt()
            Result.success(listOf(Val(sum)))
        }

        val func = Func(store, funcType, callback)

        assertFailsWith<WasmtimeException> {
            func.call(listOf(Val(1), Val(2)))
        }

        store.close()
    }

    @Test
    fun testFuncFromRaw() {
        val store = Store<Unit>(engine)
        val paramTypes = listOf(ValType.I32(), ValType.I64())
        val resultTypes = listOf(ValType.I32())
        val funcType = FuncType(paramTypes.toTypedArray(), resultTypes.toTypedArray())

        val callback: FuncCallback = { _, args ->
            val x = args[0].i32
            val y = args[1].i64
            val sum = x + y.toInt()
            Result.success(listOf(Val(sum)))
        }

        val func = Func(store, funcType, callback)
        val rawFunc = Func.fromRaw(func.toRaw(), store)
        val args = listOf(Val(1), Val(2L))
        val results = rawFunc.call(args)

        assertEquals(1, results.size)
        assertEquals(Val(3), results[0])
        
        store.close()
    }

    @Test
    fun testFuncReturnsError() {
        val store = Store<Unit>(engine)
        val paramTypes = listOf(ValType.I32())
        val resultTypes = listOf(ValType.I32())
        val funcType = FuncType(paramTypes.toTypedArray(), resultTypes.toTypedArray())

        val callback: FuncCallback = { _, args ->
            if (args[0].i32 < 0) {
                Result.failure(Exception("negative argument not allowed"))
            } else {
                Result.success(listOf(Val(args[0].i32 + 1)))
            }
        }

        val func = Func(store, funcType, callback)

        val argsValid = listOf(Val(1))
        val resultsValid = func.call(argsValid)
        assertEquals(1, resultsValid.size)
        assertEquals(Val(2), resultsValid[0])

        val argsInvalid = listOf(Val(-1))
        assertFailsWith<WasmtimeException> {
            func.call(argsInvalid)
        }

        store.close()
    }

    @Test
    fun testFuncCallNoArgs() {
        val store = Store<Unit>(engine)
        val funcType = FuncType(emptyArray(), emptyArray())

        val callback: FuncCallback = { _, _ ->
            Result.success(emptyList())
        }

        val func = Func(store, funcType, callback)

        val results = func.call()
        assertTrue(results.isEmpty())

        store.close()
    }

    @Test
    fun testFuncCallMultipleArgsAndResults() {
        val store = Store<Unit>(engine)
        val paramTypes = listOf(ValType.I32(), ValType.I64())
        val resultTypes = listOf(ValType.F32(), ValType.F64())
        val funcType = FuncType(paramTypes.toTypedArray(), resultTypes.toTypedArray())

        val callback: FuncCallback = { _, args ->
            Result.success(
                listOf(
                    Val(args[0].i32.toFloat()),
                    Val(args[1].i64.toDouble())
                )
            )
        }

        val func = Func(store, funcType, callback)

        val args = listOf(Val(42), Val(1234567890123456L))
        val results = func.call(args)
        assertEquals(2, results.size)
        assertEquals(Val(42f), results[0])
        assertEquals(Val(1234567890123456.0), results[1])

        store.close()
    }

    @Test
    fun testFuncCallMismatchedArgs() {
        val store = Store<Unit>(engine)
        val paramTypes = listOf(ValType.I32(), ValType.I32())
        val resultTypes = listOf(ValType.I32())
        val funcType = FuncType(paramTypes.toTypedArray(), resultTypes.toTypedArray())

        val callback: FuncCallback = { _, args ->
            Result.success(listOf(Val(args[0].i32 + args[1].i32)))
        }

        val func = Func(store, funcType, callback)

        val argsMismatched = listOf(Val(1), Val(2L))
        assertFailsWith<WasmtimeException> {
            func.call(argsMismatched)
        }
        
        store.close()
    }

    @Test
    fun testFuncCallCaptureValueInClosure() {
        val store = Store<Unit>(engine)
        val paramTypes = listOf(ValType.I32())
        val resultTypes = listOf(ValType.I32())
        val funcType = FuncType(paramTypes.toTypedArray(), resultTypes.toTypedArray())

        val capturedValue = 5
        val callback: FuncCallback = { _, args ->
            Result.success(listOf(Val(args[0].i32 + capturedValue)))
        }

        val func = Func(store, funcType, callback)

        val args = listOf(Val(7))
        val results = func.call(args)
        assertEquals(1, results.size)
        assertEquals(Val(12), results[0])
        
        store.close()
    }

    @Test
    fun testFuncCallCallbackCaptureClassPropertyInClosure() {
        val store = Store(engine, mutableListOf(1))
        val paramTypes = listOf(ValType.I32())
        val resultTypes = listOf(ValType.I32())
        val funcType = FuncType(paramTypes.toTypedArray(), resultTypes.toTypedArray())

        class MyTestUtil(val store: Store<MutableList<Int>>) {
            fun sumI32(args: List<Val>): List<Val> {
                val state = this.store.data!!
                state.add(args[0].i32)
                return listOf(Val(state.sum()))
            }
        }

        val myTestUtil = MyTestUtil(store)
        val callback: FuncCallback = { _, args ->
            Result.success(myTestUtil.sumI32(args))
        }

        val func = Func(store, funcType, callback)

        val args = listOf(Val(3))
        val results = func.call(args)
        assertEquals(1, results.size)
        assertEquals(Val(4), results[0])
        assertEquals(2, store.data!!.size)
        assertEquals(1, store.data!![0])
        assertEquals(3, store.data!![1])
        
        store.close()
    }
}