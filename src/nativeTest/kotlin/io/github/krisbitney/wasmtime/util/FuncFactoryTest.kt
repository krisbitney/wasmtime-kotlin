package io.github.krisbitney.wasmtime.util

import io.github.krisbitney.wasmtime.*
import io.github.krisbitney.wasmtime.wasm.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FuncFactoryTest {

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
    fun testProducerWrap() {
        val func = FuncFactory.wrap(store, ValType.I32(), ValType.I64(), ValType.I32()) {
            x: Int, y: Long -> x + y.toInt()
        }

        val args = listOf(Val(1), Val(2L))
        val results = func.call(args)

        assertEquals(1, results.size)
        assertEquals(Val(3), results[0])
    }

    @Test
    fun testConsumerWrap() {
        val func = FuncFactory.wrap(store, ValType.I32(), ValType.I32(), ValType.I32()) {
                x: Int, y: Int, z: Int -> x + y + z
        }

        val args = listOf(Val(1), Val(2), Val(3))
        val results = func.call(args)

        assertEquals(0, results.size)
    }

    @Test
    fun testProducer() {
        val func = FuncFactory.wrap(store, ValType.I32(), ValType.I64(), ValType.I32()) {
                x: Int, y: Long -> x + y.toInt()
        }
        val call = FuncFactory.producer(func, ValType.I32(), ValType.I64(), ValType.I32())

        val result = call(1, 2L)
        assertEquals(3, result)
    }

    @Test
    fun testConsumer() {
        val func = FuncFactory.wrap(store, ValType.I32(), ValType.I32(), ValType.I32()) {
                x: Int, y: Int, z: Int -> x + y + z
        }
        val accept = FuncFactory.consumer(func, ValType.I32(), ValType.I32(), ValType.I32())

        accept(1, 2, 3)
    }
}