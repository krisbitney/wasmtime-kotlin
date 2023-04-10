import io.polywrap.wasmtime.Val
import kotlinx.cinterop.*
import io.polywrap.wasmtime.wasm.ExportType
import wasmtime.*
import io.polywrap.wasmtime.wasm.ImportType
import io.polywrap.wasmtime.wasm.ValType

fun CValue<wasm_importtype_vec_t>.toList(): List<ImportType> {
    return this.useContents {
        val list = mutableListOf<ImportType>()

        for (i in 0 until size.toInt()) {
            val ptr = data?.get(i) ?: throw Error("Failed to access element at index $i")
            val importType = ImportType(ptr)
            list.add(importType)
        }

        list
    }
}

fun CValue<wasm_exporttype_vec_t>.toList(): List<ExportType> {
    return this.useContents {
        val list = mutableListOf<ExportType>()

        for (i in 0 until size.toInt()) {
            val ptr = data?.get(i) ?: throw Error("Failed to access element at index $i")
            val exportType = ExportType(ptr)
            list.add(exportType)
        }

        list
    }
}

fun CPointer<wasm_valtype_vec_t>.toList(): List<ValType<*>> {
    val size = this.pointed.size.toInt()
    val list = mutableListOf<ValType<*>>()

    for (i in 0 until size) {
        val ptr = this.pointed.data?.get(i) ?: throw Error("Failed to access element at index $i")
        val valType = ValType.fromCValue(ptr)
        list.add(valType)
    }

    return list
}

fun CArrayPointer<wasmtime_val_t>.toList(size: Int): List<Val> {
    return (0 until size).map { Val.fromCValue(this[it].ptr) }
}