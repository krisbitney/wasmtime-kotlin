package io.github.krisbitney.wasmtime.wasm

import io.github.krisbitney.wasmtime.Val
import wasmtime.wasm_valtype_kind
import kotlin.test.*

class ValTypeTest {

    @Test
    fun testFromVal() {
        val i32Val = Val(42)
        val i64Val = Val(1234567890L)
        val f32Val = Val(42.0f)
        val f64Val = Val(12345.6789)

        assertEquals(42, ValType.I32().fromVal(i32Val))
        assertEquals(1234567890L, ValType.I64().fromVal(i64Val))
        assertEquals(42.0f, ValType.F32().fromVal(f32Val))
        assertEquals(12345.6789, ValType.F64().fromVal(f64Val))
    }

    @Test
    fun testFromKind() {
        assertTrue(ValType.fromKind(ValType.Kind.I32) is ValType.I32)
        assertTrue(ValType.fromKind(ValType.Kind.I64) is ValType.I64)
        assertTrue(ValType.fromKind(ValType.Kind.F32) is ValType.F32)
        assertTrue(ValType.fromKind(ValType.Kind.F64) is ValType.F64)
        assertTrue(ValType.fromKind(ValType.Kind.ANYREF) is ValType.AnyRef)
        assertTrue(ValType.fromKind(ValType.Kind.FUNCREF) is ValType.FuncRefType)
    }

    @Test
    fun testAllocateCValue() {
        val allocatedI32 = ValType.allocateCValue(ValType.Kind.I32)
        assertNotNull(allocatedI32)
        assertNotEquals(allocatedI32.rawValue.toLong(), 0)
        val allocatedI64 = ValType.allocateCValue(ValType.Kind.I64)
        assertNotNull(allocatedI64)
        assertNotEquals(allocatedI64.rawValue.toLong(), 0)
        val allocatedF32 = ValType.allocateCValue(ValType.Kind.F32)
        assertNotNull(allocatedF32)
        assertNotEquals(allocatedF32.rawValue.toLong(), 0)
        val allocatedF64 = ValType.allocateCValue(ValType.Kind.F64)
        assertNotNull(allocatedF64)
        assertNotEquals(allocatedF64.rawValue.toLong(), 0)
        val allocatedAnyRef = ValType.allocateCValue(ValType.Kind.ANYREF)
        assertNotNull(allocatedAnyRef)
        assertNotEquals(allocatedAnyRef.rawValue.toLong(), 0)
        val allocatedFuncRef = ValType.allocateCValue(ValType.Kind.FUNCREF)
        assertNotNull(allocatedFuncRef)
        assertNotEquals(allocatedFuncRef.rawValue.toLong(), 0)

        assertEquals(wasm_valtype_kind(allocatedI32).toInt(), ValType.Kind.I32.value)
        assertEquals(wasm_valtype_kind(allocatedI64).toInt(), ValType.Kind.I64.value)
        assertEquals(wasm_valtype_kind(allocatedF32).toInt(), ValType.Kind.F32.value)
        assertEquals(wasm_valtype_kind(allocatedF64).toInt(), ValType.Kind.F64.value)
        assertEquals(wasm_valtype_kind(allocatedAnyRef).toInt(), ValType.Kind.ANYREF.value)
        assertEquals(wasm_valtype_kind(allocatedFuncRef).toInt(), ValType.Kind.FUNCREF.value)

        ValType.deleteCValue(allocatedI32)
        ValType.deleteCValue(allocatedI64)
        ValType.deleteCValue(allocatedF32)
        ValType.deleteCValue(allocatedF64)
        ValType.deleteCValue(allocatedAnyRef)
        ValType.deleteCValue(allocatedFuncRef)
    }

    @Test
    fun testFromCValue() {
        val i32CValue = ValType.allocateCValue(ValType.Kind.I32)
        val i64CValue = ValType.allocateCValue(ValType.Kind.I64)
        val f32CValue = ValType.allocateCValue(ValType.Kind.F32)
        val f64CValue = ValType.allocateCValue(ValType.Kind.F64)
        val anyRefCValue = ValType.allocateCValue(ValType.Kind.ANYREF)
        val funcRefCValue = ValType.allocateCValue(ValType.Kind.FUNCREF)

        assertTrue(ValType.fromCValue(i32CValue) is ValType.I32)
        assertTrue(ValType.fromCValue(i64CValue) is ValType.I64)
        assertTrue(ValType.fromCValue(f32CValue) is ValType.F32)
        assertTrue(ValType.fromCValue(f64CValue) is ValType.F64)
        assertTrue(ValType.fromCValue(anyRefCValue) is ValType.AnyRef)
        assertTrue(ValType.fromCValue(funcRefCValue) is ValType.FuncRefType)

        ValType.deleteCValue(i32CValue)
        ValType.deleteCValue(i64CValue)
        ValType.deleteCValue(f32CValue)
        ValType.deleteCValue(f64CValue)
        ValType.deleteCValue(anyRefCValue)
        ValType.deleteCValue(funcRefCValue)
    }

    @Test
    fun testKindFromCValue() {
        val i32CValue = ValType.allocateCValue(ValType.Kind.I32)
        val i64CValue = ValType.allocateCValue(ValType.Kind.I64)
        val f32CValue = ValType.allocateCValue(ValType.Kind.F32)
        val f64CValue = ValType.allocateCValue(ValType.Kind.F64)
        val anyRefCValue = ValType.allocateCValue(ValType.Kind.ANYREF)
        val funcRefCValue = ValType.allocateCValue(ValType.Kind.FUNCREF)

        assertEquals(ValType.Kind.I32, ValType.kindFromCValue(i32CValue))
        assertEquals(ValType.Kind.I64, ValType.kindFromCValue(i64CValue))
        assertEquals(ValType.Kind.F32, ValType.kindFromCValue(f32CValue))
        assertEquals(ValType.Kind.F64, ValType.kindFromCValue(f64CValue))
        assertEquals(ValType.Kind.ANYREF, ValType.kindFromCValue(anyRefCValue))
        assertEquals(ValType.Kind.FUNCREF, ValType.kindFromCValue(funcRefCValue))

        ValType.deleteCValue(i32CValue)
        ValType.deleteCValue(i64CValue)
        ValType.deleteCValue(f32CValue)
        ValType.deleteCValue(f64CValue)
        ValType.deleteCValue(anyRefCValue)
        ValType.deleteCValue(funcRefCValue)
    }
}