package eth.krisbitney.wasmtime.util

import eth.krisbitney.wasmtime.Val
import kotlinx.cinterop.*
import eth.krisbitney.wasmtime.wasm.ExportType
import wasmtime.*
import eth.krisbitney.wasmtime.wasm.ImportType
import eth.krisbitney.wasmtime.wasm.ValType

internal fun CValue<wasm_importtype_vec_t>.toList(): List<ImportType> {
    return this.useContents {
        val list = mutableListOf<ImportType>()

        for (i in 0 until size.toInt()) {
            val ptr = data?.get(i) ?: throw Exception("Failed to access element at index $i")
            val importType = ImportType(ptr)
            list.add(importType)
        }

        list
    }
}

internal fun CValue<wasm_exporttype_vec_t>.toList(): List<ExportType> {
    return this.useContents {
        val list = mutableListOf<ExportType>()

        for (i in 0 until size.toInt()) {
            val ptr = data?.get(i) ?: throw Exception("Failed to access element at index $i")
            val exportType = ExportType(ptr)
            list.add(exportType)
        }

        list
    }
}

internal fun CPointer<wasm_valtype_vec_t>.toList(): List<ValType<*>> {
    val size = this.pointed.size.toInt()
    val list = mutableListOf<ValType<*>>()

    for (i in 0 until size) {
        val ptr = this.pointed.data?.get(i) ?: throw Exception("Failed to access element at index $i")
        val valType = ValType.fromCValue(ptr)
        list.add(valType)
    }

    return list
}

internal fun CArrayPointer<wasmtime_val_t>.toList(size: Int): List<Val> {
    return (0 until size).map { Val.fromCValue(this[it].ptr) }
}