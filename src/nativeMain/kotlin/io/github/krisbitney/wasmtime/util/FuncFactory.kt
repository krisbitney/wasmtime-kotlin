package io.github.krisbitney.wasmtime.util

import io.github.krisbitney.wasmtime.*
import io.github.krisbitney.wasmtime.wasm.*

class FuncFactory {

    companion object {
        fun <T, R0: Any> wrap(store: Store<T>, r0: ValType<R0>, fn: () -> R0): Func {
            val funcType = FuncType(arrayOf(), arrayOf(r0))
            return Func(store, funcType) { _, _ ->
                val result = runCatching { fn() }
                if (result.isSuccess) {
                    val returned = result.getOrThrow()
                    Result.success(listOf(Val(r0, returned)))
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, R0: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            r0: ValType<R0>,
            fn: (arg0: A0) -> R0
        ): Func {
            val funcType = FuncType(arrayOf(a0), arrayOf(r0))
            return Func(store, funcType) { _, params ->
                val result = runCatching { fn(a0.fromVal(params[0])) }
                if (result.isSuccess) {
                    val returned = result.getOrThrow()
                    Result.success(listOf(Val(r0, returned)))
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, R0: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            r0: ValType<R0>,
            fn: (arg0: A0, arg1: A1) -> R0
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1), arrayOf(r0))
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1])
                    )
                }
                if (result.isSuccess) {
                    val returned = result.getOrThrow()
                    Result.success(listOf(Val(r0, returned)))
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, R0: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            r0: ValType<R0>,
            fn: (arg0: A0, arg1: A1, arg2: A2) -> R0
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2), arrayOf(r0))
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                    )
                }
                if (result.isSuccess) {
                    val returned = result.getOrThrow()
                    Result.success(listOf(Val(r0, returned)))
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any, R0: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            r0: ValType<R0>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3) -> R0
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3), arrayOf(r0))
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                    )
                }
                if (result.isSuccess) {
                    val returned = result.getOrThrow()
                    Result.success(listOf(Val(r0, returned)))
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, R0: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            r0: ValType<R0>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4) -> R0
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4), arrayOf(r0))
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                        a4.fromVal(params[4]),
                    )
                }
                if (result.isSuccess) {
                    val returned = result.getOrThrow()
                    Result.success(listOf(Val(r0, returned)))
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, R0: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            r0: ValType<R0>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5) -> R0
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4, a5), arrayOf(r0))
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                        a4.fromVal(params[4]),
                        a5.fromVal(params[5]),
                    )
                }
                if (result.isSuccess) {
                    val returned = result.getOrThrow()
                    Result.success(listOf(Val(r0, returned)))
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any, R0: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>,
            r0: ValType<R0>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6) -> R0
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4, a5, a6), arrayOf(r0))
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                        a4.fromVal(params[4]),
                        a5.fromVal(params[5]),
                        a6.fromVal(params[6]),
                    )
                }
                if (result.isSuccess) {
                    val returned = result.getOrThrow()
                    Result.success(listOf(Val(r0, returned)))
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any, A7: Any, R0: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>,
            a7: ValType<A7>,
            r0: ValType<R0>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7) -> R0
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4, a5, a6, a7), arrayOf(r0))
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                        a4.fromVal(params[4]),
                        a5.fromVal(params[5]),
                        a6.fromVal(params[6]),
                        a7.fromVal(params[7]),
                    )
                }
                if (result.isSuccess) {
                    val returned = result.getOrThrow()
                    Result.success(listOf(Val(r0, returned)))
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any, A7: Any, A8: Any, R0: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>,
            a7: ValType<A7>,
            a8: ValType<A8>,
            r0: ValType<R0>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7, arg8: A8) -> R0
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4, a5, a6, a7, a8), arrayOf(r0))
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                        a4.fromVal(params[4]),
                        a5.fromVal(params[5]),
                        a6.fromVal(params[6]),
                        a7.fromVal(params[7]),
                        a8.fromVal(params[8]),
                    )
                }
                if (result.isSuccess) {
                    val returned = result.getOrThrow()
                    Result.success(listOf(Val(r0, returned)))
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T> wrap(store: Store<T>, fn: () -> Unit): Func {
            val funcType = FuncType(arrayOf(), arrayOf())
            return Func(store, funcType) { _, _ ->
                val result = runCatching { fn() }
                if (result.isSuccess) {
                    Result.success(listOf())
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any> wrap(store: Store<T>, a0: ValType<A0>, fn: (arg0: A0) -> Unit): Func {
            val funcType = FuncType(arrayOf(a0), arrayOf())
            return Func(store, funcType) { _, params ->
                val result = runCatching { fn(a0.fromVal(params[0])) }
                if (result.isSuccess) {
                    Result.success(listOf())
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            fn: (arg0: A0, arg1: A1) -> Unit
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1), arrayOf())
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                    )
                }
                if (result.isSuccess) {
                    Result.success(listOf())
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            fn: (arg0: A0, arg1: A1, arg2: A2) -> Unit
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2), arrayOf())
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                    )
                }
                if (result.isSuccess) {
                    Result.success(listOf())
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3) -> Unit
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3), arrayOf())
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                    )
                }
                if (result.isSuccess) {
                    Result.success(listOf())
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4) -> Unit
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4), arrayOf())
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                        a4.fromVal(params[4]),
                    )
                }
                if (result.isSuccess) {
                    Result.success(listOf())
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5) -> Unit
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4, a5), arrayOf())
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                        a4.fromVal(params[4]),
                        a5.fromVal(params[5]),
                    )
                }
                if (result.isSuccess) {
                    Result.success(listOf())
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6) -> Unit
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4, a5, a6), arrayOf())
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                        a4.fromVal(params[4]),
                        a5.fromVal(params[5]),
                        a6.fromVal(params[6]),
                    )
                }
                if (result.isSuccess) {
                    Result.success(listOf())
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any, A7: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>,
            a7: ValType<A7>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7) -> Unit
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4, a5, a6, a7), arrayOf())
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                        a4.fromVal(params[4]),
                        a5.fromVal(params[5]),
                        a6.fromVal(params[6]),
                        a7.fromVal(params[7]),
                    )
                }
                if (result.isSuccess) {
                    Result.success(listOf())
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any, A7: Any, A8: Any> wrap(
            store: Store<T>,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>,
            a7: ValType<A7>,
            a8: ValType<A8>,
            fn: (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7, arg8: A8) -> Unit
        ): Func {
            val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4, a5, a6, a7, a8), arrayOf())
            return Func(store, funcType) { _, params ->
                val result = runCatching {
                    fn(
                        a0.fromVal(params[0]),
                        a1.fromVal(params[1]),
                        a2.fromVal(params[2]),
                        a3.fromVal(params[3]),
                        a4.fromVal(params[4]),
                        a5.fromVal(params[5]),
                        a6.fromVal(params[6]),
                        a7.fromVal(params[7]),
                        a8.fromVal(params[8]),
                    )
                }
                if (result.isSuccess) {
                    Result.success(listOf())
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        }

        fun <R0: Any> producer(fn: Func, r0: ValType<R0>): () -> R0 {
            return { r0.fromVal(fn.call()[0]) }
        }

        fun <A0: Any, R0: Any> producer(fn: Func, a0: ValType<A0>, r0: ValType<R0>): (a0: A0) -> R0 {
            return { arg0: A0 ->
                val vals = listOf(Val(a0, arg0))
                r0.fromVal(fn.call(vals)[0])
            }
        }

        fun <A0: Any, A1: Any, R0: Any> producer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            r0: ValType<R0>
        ): (a0: A0, a1: A1) -> R0 {
            return { arg0: A0, arg1: A1 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1))
                r0.fromVal(fn.call(vals)[0])
            }
        }

        fun <A0: Any, A1: Any, A2: Any, R0: Any> producer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            r0: ValType<R0>
        ): (arg0: A0, arg1: A1, arg2: A2) -> R0 {
            return { arg0: A0, arg1: A1, arg2: A2 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2))
                r0.fromVal(fn.call(vals)[0])
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any, R0: Any> producer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            r0: ValType<R0>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3) -> R0 {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3))
                r0.fromVal(fn.call(vals)[0])
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, R0: Any> producer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            r0: ValType<R0>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4) -> R0 {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4))
                r0.fromVal(fn.call(vals)[0])
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, R0: Any> producer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            r0: ValType<R0>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5) -> R0 {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4), Val(a5, arg5))
                r0.fromVal(fn.call(vals)[0])
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any, R0: Any> producer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>,
            r0: ValType<R0>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6) -> R0 {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4), Val(a5, arg5), Val(a6, arg6))
                r0.fromVal(fn.call(vals)[0])
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any, A7: Any, R0: Any> producer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>,
            a7: ValType<A7>,
            r0: ValType<R0>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7) -> R0 {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4), Val(a5, arg5), Val(a6, arg6), Val(a7, arg7))
                r0.fromVal(fn.call(vals)[0])
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any, A7: Any, A8: Any, R0: Any> producer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>,
            a7: ValType<A7>,
            a8: ValType<A8>,
            r0: ValType<R0>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7, arg8: A8) -> R0 {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7, arg8: A8 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4), Val(a5, arg5), Val(a6, arg6), Val(a7, arg7), Val(a8, arg8))
                r0.fromVal(fn.call(vals)[0])
            }
        }

        fun consumer(fn: Func): () -> Unit {
            return { fn.call() }
        }

        fun <A0: Any> consumer(fn: Func, a0: ValType<A0>): (arg0: A0) -> Unit {
            return { arg0: A0 ->
                val vals = listOf(Val(a0, arg0))
                fn.call(vals)
            }
        }

        fun <A0: Any, A1: Any> consumer(fn: Func, a0: ValType<A0>, a1: ValType<A1>): (arg0: A0, arg1: A1) -> Unit {
            return { arg0: A0, arg1: A1 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1))
                fn.call(vals)
            }
        }

        fun <A0: Any, A1: Any, A2: Any> consumer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>
        ): (arg0: A0, arg1: A1, arg2: A2) -> Unit {
            return { arg0: A0, arg1: A1, arg2: A2 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2))
                fn.call(vals)
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any> consumer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3) -> Unit {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3))
                fn.call(vals)
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any> consumer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4) -> Unit {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4))
                fn.call(vals)
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any> consumer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5) -> Unit {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4), Val(a5, arg5))
                fn.call(vals)
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any> consumer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6) -> Unit {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4), Val(a5, arg5), Val(a6, arg6))
                fn.call(vals)
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any, A7: Any> consumer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>,
            a7: ValType<A7>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7) -> Unit {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4), Val(a5, arg5), Val(a6, arg6), Val(a7, arg7))
                fn.call(vals)
            }
        }

        fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, A6: Any, A7: Any, A8: Any> consumer(
            fn: Func,
            a0: ValType<A0>,
            a1: ValType<A1>,
            a2: ValType<A2>,
            a3: ValType<A3>,
            a4: ValType<A4>,
            a5: ValType<A5>,
            a6: ValType<A6>,
            a7: ValType<A7>,
            a8: ValType<A8>
        ): (arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7, arg8: A8) -> Unit {
            return { arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5, arg6: A6, arg7: A7, arg8: A8 ->
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4), Val(a5, arg5), Val(a6, arg6), Val(a7, arg7), Val(a8, arg8))
                fn.call(vals)
            }
        }
    }
}