package io.github.krisbitney.wasmtime

import io.github.krisbitney.wasmtime.wasm.ValType
import kotlinx.cinterop.*
import platform.posix.memcpy
import wasmtime.*
import kotlin.test.*

class ValTest {

    @Test
    fun testConstructors() = memScoped {
        val i32Val = Val(42)
        assertEquals(Val.Kind.I32, i32Val.kind)
        assertEquals(42, i32Val.value)

        val i64Val = Val(123456789L)
        assertEquals(Val.Kind.I64, i64Val.kind)
        assertEquals(123456789L, i64Val.value)

        val f32Val = Val(3.14f)
        assertEquals(Val.Kind.F32, f32Val.kind)
        assertEquals(3.14f, f32Val.value)

        val f64Val = Val(3.141592)
        assertEquals(Val.Kind.F64, f64Val.kind)
        assertEquals(3.141592, f64Val.value)

        val mockWasmtimeV128: wasmtime_v128 = alloc<UByteVarOf<UByte>>().ptr
        mockWasmtimeV128.pointed.value = 0x42u
        val v128Val = Val(mockWasmtimeV128)
        assertEquals(Val.Kind.V128, v128Val.kind)
        assertEquals(mockWasmtimeV128, v128Val.value)

        // TODO: Figure out how to mock wasmtime_funcref_t
//        val funcrefVal = Val(mockWasmtimeFuncT)
//        assertEquals(Val.Kind.FUNCREF, funcrefVal.kind)
//        assertEquals(mockWasmtimeFuncT, funcrefVal.value)

        val externrefVal = Val(ExternRef(42))
        assertEquals(Val.Kind.EXTERNREF, externrefVal.kind)
        assertEquals(42, externrefVal.externref.data<Int>())
    }

    @Test
    fun testValTypeConstructor() {
        assertFailsWith<IllegalArgumentException> { Val(ValType.I32(), 123L) }
        assertFailsWith<IllegalArgumentException> { Val(ValType.I64(), 123) }
        assertFailsWith<IllegalArgumentException> { Val(ValType.F32(), 123.0) }
        assertFailsWith<IllegalArgumentException> { Val(ValType.F64(), 123.0f) }
        assertFailsWith<IllegalArgumentException> { Val(ValType.FuncRefType(), 123) }
        assertFailsWith<IllegalArgumentException> { Val(ValType.AnyRef(), 123) }
    }

    @Test
    fun testValTypeConstructorInvalidValues() {
        assertFailsWith<IllegalArgumentException> { Val(ValType.I32(), 123L) }
        assertFailsWith<IllegalArgumentException> { Val(ValType.I64(), 123) }
        assertFailsWith<IllegalArgumentException> { Val(ValType.F32(), 123.0) }
        assertFailsWith<IllegalArgumentException> { Val(ValType.F64(), 123.0f) }
        assertFailsWith<IllegalArgumentException> { Val(ValType.FuncRefType(), 123) }
        assertFailsWith<IllegalArgumentException> { Val(ValType.AnyRef(), 123) }
    }

    @Test
    fun testValueAccessors() {
        val i32Val = Val(42)
        assertEquals(42, i32Val.i32)
        assertFailsWith<IllegalStateException> { i32Val.i64 }
        assertFailsWith<IllegalStateException> { i32Val.f32 }
        assertFailsWith<IllegalStateException> { i32Val.f64 }
        assertFailsWith<IllegalStateException> { i32Val.v128 }
        assertFailsWith<IllegalStateException> { i32Val.funcref }
        assertFailsWith<IllegalStateException> { i32Val.externref }
    }

    @Test
    fun testFromCValueI32() = memScoped {
        val wasmtimeVal = alloc<wasmtime_val_t>()
        wasmtimeVal.kind = WASMTIME_I32.toUByte()
        wasmtimeVal.of.i32 = 42
        val value = Val.fromCValue(wasmtimeVal.ptr)
        assertEquals(Val.Kind.I32, value.kind)
        assertEquals(42, value.i32)
    }

    @Test
    fun testFromCValueI64() = memScoped {
        val wasmtimeVal = alloc<wasmtime_val_t>()
        wasmtimeVal.kind = WASMTIME_I64.toUByte()
        wasmtimeVal.of.i64 = 42
        val value = Val.fromCValue(wasmtimeVal.ptr)
        assertEquals(Val.Kind.I64, value.kind)
        assertEquals(42, value.i64)
    }

    @Test
    fun testFromCValueF32() = memScoped {
        val wasmtimeVal = alloc<wasmtime_val_t>()
        wasmtimeVal.kind = WASMTIME_F32.toUByte()
        wasmtimeVal.of.f32 = 42.0f
        val value = Val.fromCValue(wasmtimeVal.ptr)
        assertEquals(Val.Kind.F32, value.kind)
        assertEquals(42.0f, value.f32)
    }

    @Test
    fun testFromCValueF64() = memScoped {
        val wasmtimeVal = alloc<wasmtime_val_t>()
        wasmtimeVal.kind = WASMTIME_F64.toUByte()
        wasmtimeVal.of.f64 = 42.0
        val value = Val.fromCValue(wasmtimeVal.ptr)
        assertEquals(Val.Kind.F64, value.kind)
        assertEquals(42.0, value.f64)
    }

    @Test
    fun testFromCValueV128() = memScoped {
        val mockWasmtimeV128: wasmtime_v128 = alloc<UByteVarOf<UByte>>().ptr
        mockWasmtimeV128.pointed.value = 0x42u

        val wasmtimeVal = alloc<wasmtime_val_t>()
        wasmtimeVal.kind = WASMTIME_V128.toUByte()
        memcpy(wasmtimeVal.of.v128, mockWasmtimeV128, 16)

        val value = Val.fromCValue(wasmtimeVal.ptr)
        assertEquals(Val.Kind.V128, value.kind)
        assertEquals(wasmtimeVal.of.v128, value.v128)
    }

    @Test
    fun testFromCValueExternRef() = memScoped {
        val mockWasmtimeExternRef = ExternRef(42)

        val wasmtimeVal = alloc<wasmtime_val_t>()
        wasmtimeVal.kind = WASMTIME_EXTERNREF.toUByte()
        wasmtimeVal.of.externref = mockWasmtimeExternRef.externRef

        val value = Val.fromCValue(wasmtimeVal.ptr)
        assertEquals(Val.Kind.EXTERNREF, value.kind)
        assertEquals(wasmtimeVal.of.externref, value.externref.externRef)

        mockWasmtimeExternRef.close()
    }

    // TODO add fromCValue test for funcref

    @Test
    fun testKindFromCValue() {
        memScoped {
            val wasmtimeVal = alloc<wasmtime_val_t>()
            wasmtimeVal.kind = WASMTIME_I32.toUByte()
            val kind = Val.kindFromCValue(wasmtimeVal.ptr)
            assertEquals(Val.Kind.I32, kind)
        }
    }

    @Test
    fun testDeleteCValue() {
        val wasmtimeVal = nativeHeap.alloc<wasmtime_val_t>()
        wasmtimeVal.kind = WASMTIME_I32.toUByte()
        Val.deleteCValue(wasmtimeVal.ptr)
    }

    @Test
    fun testAllocateCValueI32() {
        val value = Val(Val.Kind.I32, 42)
        val wasmtimeVal = Val.allocateCValue(value)
        assertEquals(WASMTIME_I32.toUByte(), wasmtimeVal.pointed.kind)
        assertEquals(42, wasmtimeVal.pointed.of.i32)
        Val.deleteCValue(wasmtimeVal)
    }

    @Test
    fun testAllocateCValueI64() {
        val value = Val(Val.Kind.I64, 42L)
        val wasmtimeVal = Val.allocateCValue(value)
        assertEquals(WASMTIME_I64.toUByte(), wasmtimeVal.pointed.kind)
        assertEquals(42L, wasmtimeVal.pointed.of.i64)
        Val.deleteCValue(wasmtimeVal)
    }

    @Test
    fun testAllocateCValueF32() {
        val value = Val(Val.Kind.F32, 42.0f)
        val wasmtimeVal = Val.allocateCValue(value)
        assertEquals(WASMTIME_F32.toUByte(), wasmtimeVal.pointed.kind)
        assertEquals(42.0f, wasmtimeVal.pointed.of.f32)
        Val.deleteCValue(wasmtimeVal)
    }

    @Test
    fun testAllocateCValueF64() {
        val value = Val(Val.Kind.F64, 42.0)
        val wasmtimeVal = Val.allocateCValue(value)
        assertEquals(WASMTIME_F64.toUByte(), wasmtimeVal.pointed.kind)
        assertEquals(42.0, wasmtimeVal.pointed.of.f64)
        Val.deleteCValue(wasmtimeVal)
    }

    @Test
    fun testAllocateCValueV128() = memScoped {
        val v128: wasmtime_v128 = allocArray<UByteVar>(16)
        v128.pointed.value = 0x42u
        val value = Val(Val.Kind.V128, v128)
        val wasmtimeVal = Val.allocateCValue(value)
        assertEquals(WASMTIME_V128.toUByte(), wasmtimeVal.pointed.kind)
        assertEquals(v128.pointed.value, wasmtimeVal.pointed.of.v128.pointed.value)
    }

    @Test
    fun testAllocateCValueExternRef() {
        val value = Val(Val.Kind.EXTERNREF, ExternRef(42))
        val wasmtimeVal = Val.allocateCValue(value)
        assertEquals(WASMTIME_EXTERNREF.toUByte(), wasmtimeVal.pointed.kind)
        assertEquals(value.externref.externRef, wasmtimeVal.pointed.of.externref)
        Val.deleteCValue(wasmtimeVal)
    }

    // TODO add allocateCValue test for funcref

    @Test
    fun testCopyCVal() {
        memScoped {
            val wasmtimeVal = alloc<wasmtime_val_t>()
            wasmtimeVal.kind = WASMTIME_I32.toUByte()
            wasmtimeVal.of.i32 = 42
            val copy = Val.copyCVal(wasmtimeVal.ptr)
            assertEquals(WASMTIME_I32.toUByte(), copy.pointed.kind)
            assertEquals(42, copy.pointed.of.i32)
            assertNotSame(wasmtimeVal.ptr, copy)
        }
    }
}
