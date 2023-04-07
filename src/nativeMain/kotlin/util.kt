import kotlinx.cinterop.*
import wasm.WasmExportType
import wasmtime.*
import wasm.WasmImportType
import wasm.WasmValType

fun CValue<wasm_importtype_vec_t>.toList(): List<WasmImportType> {
    return this.useContents {
        val list = mutableListOf<WasmImportType>()

        for (i in 0 until size.toInt()) {
            val ptr = data?.get(i) ?: throw Error("Failed to access element at index $i")
            val importType = WasmImportType(ptr)
            list.add(importType)
        }

        list
    }
}

fun CValue<wasm_exporttype_vec_t>.toList(): List<WasmExportType> {
    return this.useContents {
        val list = mutableListOf<WasmExportType>()

        for (i in 0 until size.toInt()) {
            val ptr = data?.get(i) ?: throw Error("Failed to access element at index $i")
            val exportType = WasmExportType(ptr)
            list.add(exportType)
        }

        list
    }
}

fun CPointer<wasm_valtype_vec_t>.toList(): List<WasmValType> {
    val size = this.pointed.size.toInt()
    val list = mutableListOf<WasmValType>()

    for (i in 0 until size) {
        val ptr = this.pointed.data?.get(i) ?: throw Error("Failed to access element at index $i")
        val valType = WasmValType(ptr)
        list.add(valType)
    }

    return list
}

fun CArrayPointer<wasmtime_val_t>.toList(size: Int): List<Val> {
    return (0 until size).map { Val(this[it].ptr) }
}