package eth.krisbitney.wasmtime.util

import eth.krisbitney.wasmtime.Extern
import kotlinx.cinterop.*
import platform.posix.memcpy
import platform.posix.size_t
import wasmtime.wasmtime_extern_t

/**
 * Convert List<Extern> to CValuesRef<wasmtime_extern_t>.
 * This method allocates memory on the native heap.
 * Caller owns the returned C value and must delete from native heap when done.
 * A CArrayPointer<wasmtime_extern_t> is also returned to allow for freeing the memory allocated.
 *
 * @return A Pair of CValuesRef<wasmtime_extern_t> and CArrayPointer<wasmtime_extern_t>.
 */
fun List<Extern>.toCValuesRef(): Pair<CValuesRef<wasmtime_extern_t>, CArrayPointer<wasmtime_extern_t>> {
    val externPtrs = this.map { Extern.allocateCValue(it) }
    val cExternsArray: CArrayPointer<wasmtime_extern_t> = nativeHeap.allocArray(this.size)
    val cExternSize: size_t = sizeOf<wasmtime_extern_t>().convert()
    for (i in this.indices) {
        memcpy(cExternsArray[i].ptr, externPtrs[i], cExternSize)
        Extern.deleteCValue(externPtrs[i])
    }
    return cExternsArray to cExternsArray
}