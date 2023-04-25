package io.github.krisbitney.wasmtime.util

import io.github.krisbitney.wasmtime.*
import io.github.krisbitney.wasmtime.wasm.*

class FuncFactory {

    fun <T, R0: Any> wrap(store: Store<T>, r0: ValType<R0>, func: Function0<R0>): Func {
        val funcType = FuncType(arrayOf(), arrayOf(r0))
        return Func(store, funcType) { _, _ ->
            val result = runCatching { func.call() }
            if (result.isSuccess) {
                val returned = result.getOrThrow()
                Result.success(listOf(Val(r0, returned)))
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        }
    }

    fun <T, A0: Any, R0: Any> wrap(store: Store<T>, a0: ValType<A0>, r0: ValType<R0>, func: Function1<A0, R0>): Func {
        val funcType = FuncType(arrayOf(a0), arrayOf(r0))
        return Func(store, funcType) { _, params ->
            val result = runCatching { func.call(a0.fromVal(params[0])) }
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
        func: Function2<A0, A1, R0>
    ): Func {
        val funcType = FuncType(arrayOf(a0, a1), arrayOf(r0))
        return Func(store, funcType) { _, params ->
            val result = runCatching {
                func.call(
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
        func: Function3<A0, A1, A2, R0>
    ): Func {
        val funcType = FuncType(arrayOf(a0, a1, a2), arrayOf(r0))
        return Func(store, funcType) { _, params ->
            val result = runCatching {
                func.call(
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
        func: Function4<A0, A1, A2, A3, R0>
    ): Func {
        val funcType = FuncType(arrayOf(a0, a1, a2, a3), arrayOf(r0))
        return Func(store, funcType) { _, params ->
            val result = runCatching {
                func.call(
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
        func: Function5<A0, A1, A2, A3, A4, R0>
    ): Func {
        val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4), arrayOf(r0))
        return Func(store, funcType) { _, params ->
            val result = runCatching {
                func.call(
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
        func: Function6<A0, A1, A2, A3, A4, A5, R0>
    ): Func {
        val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4, a5), arrayOf(r0))
        return Func(store, funcType) { _, params ->
            val result = runCatching {
                func.call(
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

    fun <T>wrap(store: Store<T>, func: Consumer0): Func {
        val funcType = FuncType(arrayOf(), arrayOf())
        return Func(store, funcType) { _, _ ->
            val result = runCatching { func.accept() }
            if (result.isSuccess) {
                Result.success(listOf())
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        }
    }

    fun <T, A0: Any> wrap(store: Store<T>, a0: ValType<A0>, func: Consumer1<A0>): Func {
        val funcType = FuncType(arrayOf(a0), arrayOf())
        return Func(store, funcType) { _, params ->
            val result = runCatching { func.accept(a0.fromVal(params[0])) }
            if (result.isSuccess) {
                Result.success(listOf())
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        }
    }

    fun <T, A0: Any, A1: Any> wrap(store: Store<T>, a0: ValType<A0>, a1: ValType<A1>, func: Consumer2<A0, A1>): Func {
        val funcType = FuncType(arrayOf(a0, a1), arrayOf())
        return Func(store, funcType) { _, params ->
            val result = runCatching {
                func.accept(
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

    fun <T, A0: Any, A1: Any, A2: Any> wrap(store: Store<T>, a0: ValType<A0>, a1: ValType<A1>,a2: ValType<A2>, func: Consumer3<A0, A1, A2>): Func {
        val funcType = FuncType(arrayOf(a0, a1, a2), arrayOf())
        return Func(store, funcType) { _, params ->
            val result = runCatching {
                func.accept(
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

    fun <T, A0: Any, A1: Any, A2: Any, A3: Any> wrap(store: Store<T>, a0: ValType<A0>, a1: ValType<A1>,a2: ValType<A2>, a3: ValType<A3>, func: Consumer4<A0, A1, A2, A3>): Func {
        val funcType = FuncType(arrayOf(a0, a1, a2, a3), arrayOf())
        return Func(store, funcType) { _, params ->
            val result = runCatching {
                func.accept(
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

    fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any> wrap(store: Store<T>, a0: ValType<A0>, a1: ValType<A1>,a2: ValType<A2>, a3: ValType<A3>, a4: ValType<A4>, func: Consumer5<A0, A1, A2, A3, A4>): Func {
        val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4), arrayOf())
        return Func(store, funcType) { _, params ->
            val result = runCatching {
                func.accept(
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

    fun <T, A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any> wrap(store: Store<T>, a0: ValType<A0>, a1: ValType<A1>,a2: ValType<A2>, a3: ValType<A3>, a4: ValType<A4>, a5: ValType<A5>, func: Consumer6<A0, A1, A2, A3, A4, A5>): Func {
        val funcType = FuncType(arrayOf(a0, a1, a2, a3, a4, a5), arrayOf())
        return Func(store, funcType) { _, params ->
            val result = runCatching {
                func.accept(
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

    fun <R0: Any> func(fn: Func, r0: ValType<R0>): Function0<R0> {
        return object : Function0<R0> {
            override fun call(): R0 {
                return r0.fromVal(fn.call()[0])
            }
        }
    }

    fun <A0: Any, R0: Any> func(fn: Func, a0: ValType<A0>, r0: ValType<R0>): Function1<A0, R0> {
        return object : Function1<A0, R0> {
            override fun call(arg0: A0): R0 {
                val vals = listOf(Val(a0, arg0))
                return r0.fromVal(fn.call(vals)[0])
            }
        }
    }

    fun <A0: Any, A1: Any, R0: Any> func(
        fn: Func,
        a0: ValType<A0>,
        a1: ValType<A1>,
        r0: ValType<R0>
    ): Function2<A0, A1, R0> {
        return object : Function2<A0, A1, R0> {
            override fun call(arg0: A0, arg1: A1): R0 {
                val vals = listOf(Val(a0, arg0), Val(a1, arg1))
                return r0.fromVal(fn.call(vals)[0])
            }
        }
    }

    fun <A0: Any, A1: Any, A2: Any, R0: Any> func(
        fn: Func,
        a0: ValType<A0>,
        a1: ValType<A1>,
        a2: ValType<A2>,
        r0: ValType<R0>
    ): Function3<A0, A1, A2, R0> {
        return object : Function3<A0, A1, A2, R0> {
            override fun call(arg0: A0, arg1: A1, arg2: A2): R0 {
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2))
                return r0.fromVal(fn.call(vals)[0])
            }
        }
    }

    fun <A0: Any, A1: Any, A2: Any, A3: Any, R0: Any> func(
        fn: Func,
        a0: ValType<A0>,
        a1: ValType<A1>,
        a2: ValType<A2>,
        a3: ValType<A3>,
        r0: ValType<R0>
    ): Function4<A0, A1, A2, A3, R0> {
        return object : Function4<A0, A1, A2, A3, R0> {
            override fun call(arg0: A0, arg1: A1, arg2: A2, arg3: A3): R0 {
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3))
                return r0.fromVal(fn.call(vals)[0])
            }
        }
    }

    fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, R0: Any> func(
        fn: Func,
        a0: ValType<A0>,
        a1: ValType<A1>,
        a2: ValType<A2>,
        a3: ValType<A3>,
        a4: ValType<A4>,
        r0: ValType<R0>
    ): Function5<A0, A1, A2, A3, A4, R0> {
        return object : Function5<A0, A1, A2, A3, A4, R0> {
            override fun call(arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4): R0 {
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4))
                return r0.fromVal(fn.call(vals)[0])
            }
        }
    }

    fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any, A5: Any, R0: Any> func(
        fn: Func,
        a0: ValType<A0>,
        a1: ValType<A1>,
        a2: ValType<A2>,
        a3: ValType<A3>,
        a4: ValType<A4>,
        a5: ValType<A5>,
        r0: ValType<R0>
    ): Function6<A0, A1, A2, A3, A4, A5, R0> {
        return object : Function6<A0, A1, A2, A3, A4, A5, R0> {
            override fun call(arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5): R0 {
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4), Val(a5, arg5))
                return r0.fromVal(fn.call(vals)[0])
            }
        }
    }

    fun consumer(fn: Func): Consumer0 {
        return object : Consumer0 {
            override fun accept() { fn.call() }
        }
    }

    fun <A0: Any> consumer(fn: Func, a0: ValType<A0>): Consumer1<A0> {
        return object : Consumer1<A0> {
            override fun accept(arg0: A0) {
                val vals = listOf(Val(a0, arg0))
                fn.call(vals)
            }
        }
    }

    fun <A0: Any, A1: Any> consumer(fn: Func, a0: ValType<A0>, a1: ValType<A1>): Consumer2<A0, A1> {
        return object : Consumer2<A0, A1> {
            override fun accept(arg0: A0, arg1: A1) {
                val vals = listOf(Val(a0, arg0), Val(a1, arg1))
                fn.call(vals)
            }
        }
    }

    fun <A0: Any, A1: Any, A2: Any> consumer(
        fn: Func,
        a0: ValType<A0>,
        a1: ValType<A1>,
        a2: ValType<A2>
    ): Consumer3<A0, A1, A2> {
        return object : Consumer3<A0, A1, A2> {
            override fun accept(arg0: A0, arg1: A1, arg2: A2) {
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2))
                fn.call(vals)
            }
        }
    }

    fun <A0: Any, A1: Any, A2: Any, A3: Any> consumer(
        fn: Func,
        a0: ValType<A0>,
        a1: ValType<A1>,
        a2: ValType<A2>,
        a3: ValType<A3>
    ): Consumer4<A0, A1, A2, A3> {
        return object : Consumer4<A0, A1, A2, A3> {
            override fun accept(arg0: A0, arg1: A1, arg2: A2, arg3: A3) {
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3))
                fn.call(vals)
            }
        }
    }

    fun <A0: Any, A1: Any, A2: Any, A3: Any, A4: Any> consumer(
        fn: Func,
        a0: ValType<A0>,
        a1: ValType<A1>,
        a2: ValType<A2>,
        a3: ValType<A3>,
        a4: ValType<A4>
    ): Consumer5<A0, A1, A2, A3, A4> {
        return object : Consumer5<A0, A1, A2, A3, A4> {
            override fun accept(arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4) {
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4))
                fn.call(vals)
            }
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
    ): Consumer6<A0, A1, A2, A3, A4, A5> {
        return object : Consumer6<A0, A1, A2, A3, A4, A5> {
            override fun accept(arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5) {
                val vals = listOf(Val(a0, arg0), Val(a1, arg1), Val(a2, arg2), Val(a3, arg3), Val(a4, arg4), Val(a5, arg5))
                fn.call(vals)
            }
        }
    }

    interface Function0<R0> {
        fun call(): R0
    }

    interface Function1<A0, R0> {
        fun call(arg0: A0): R0
    }

    interface Function2<A0, A1, R0> {
        fun call(arg0: A0, arg1: A1): R0
    }

    interface Function3<A0, A1, A2, R0> {
        fun call(arg0: A0, arg1: A1, arg2: A2): R0
    }

    interface Function4<A0, A1, A2, A3, R0> {
        fun call(arg0: A0, arg1: A1, arg2: A2, arg3: A3): R0
    }

    interface Function5<A0, A1, A2, A3, A4, R0> {
        fun call(arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4): R0
    }

    interface Function6<A0, A1, A2, A3, A4, A5, R0> {
        fun call(arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5): R0
    }

    interface Consumer0 {
        fun accept()
    }

    interface Consumer1<A0> {
        fun accept(arg0: A0)
    }

    interface Consumer2<A0, A1> {
        fun accept(arg0: A0, arg1: A1)
    }

    interface Consumer3<A0, A1, A2> {
        fun accept(arg0: A0, arg1: A1, arg2: A2)
    }

    interface Consumer4<A0, A1, A2, A3> {
        fun accept(arg0: A0, arg1: A1, arg2: A2, arg3: A3)
    }

    interface Consumer5<A0, A1, A2, A3, A4> {
        fun accept(arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4)
    }

    interface Consumer6<A0, A1, A2, A3, A4, A5> {
        fun accept(arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5)
    }
}
