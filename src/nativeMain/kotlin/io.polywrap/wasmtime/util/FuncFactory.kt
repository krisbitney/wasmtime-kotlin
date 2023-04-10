package io.polywrap.wasmtime.util

import io.polywrap.wasmtime.*
import io.polywrap.wasmtime.wasm.*

class FuncFactory {

    fun <T, R0: Any> wrap(store: Store<T>, r0: ValType<R0>, func: Function0<R0>): Func {
        val funcType = Func.wasmFunctype(null, arrayOf(r0))
        return Func(store, funcType) { caller, params ->
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
        val funcType = Func.wasmFunctype(arrayOf(a0), arrayOf(r0))
        return Func(store, funcType) { caller, params ->
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
        val funcType = Func.wasmFunctype(arrayOf(a0, a1), arrayOf(r0))
        return Func(store, funcType) { caller, params ->
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
        val funcType = Func.wasmFunctype(arrayOf(a0, a1, a2), arrayOf(r0))
        return Func(store, funcType) { caller, params ->
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

    fun <T>wrap(store: Store<T>, func: Consumer0): Func {
        val funcType = Func.wasmFunctype(null, null)
        return Func(store, funcType) { caller, params ->
            val result = runCatching { func.accept() }
            if (result.isSuccess) {
                Result.success(listOf())
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        }
    }

    fun <T, A0: Any> wrap(store: Store<T>, a0: ValType<A0>, func: Consumer1<A0>): Func {
        val funcType = Func.wasmFunctype(arrayOf(a0), null)
        return Func(store, funcType) { caller, params ->
            val result = runCatching { func.accept(a0.fromVal(params[0])) }
            if (result.isSuccess) {
                Result.success(listOf())
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        }
    }

    fun <T, A0: Any, A1: Any> wrap(store: Store<T>, a0: ValType<A0>, a1: ValType<A1>, func: Consumer2<A0, A1>): Func {
        val funcType = Func.wasmFunctype(arrayOf(a0, a1), null)
        return Func(store, funcType) { caller, params ->
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
        val funcType = Func.wasmFunctype(arrayOf(a0, a1, a2), null)
        return Func(store, funcType) { caller, params ->
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
}
