package eth.krisbitney.wasmtime

import kotlin.test.*

class ExternRefTests {

    @Test
    fun testCreateExternRefWithData() {
        val data = "Sample data"
        val externRef = ExternRef(data)

        assertNotNull(externRef, "ExternRef should be created successfully")
        assertEquals(data, externRef.data<String>(), "Wrapped data should match the original data")

        externRef.close()
    }

    @Test
    fun testCreateExternRefWithNullData() {
        val externRef = ExternRef<String>(null)

        assertNotNull(externRef, "ExternRef should be created successfully")
        assertNull(externRef.data<String>(), "Wrapped data should be null")

        externRef.close()
    }

    @Test
    fun testCloneExternRef() {
        val data = "Sample data"
        val externRef = ExternRef(data)
        val clonedRef = externRef.clone()

        assertNotNull(clonedRef, "Cloned ExternRef should be created successfully")
        assertEquals(data, clonedRef.data<String>(), "Wrapped data in the cloned ExternRef should match the original data")

        externRef.close()
        clonedRef.close()
    }

    @Test
    fun testRawConversion() {
        val engine = Engine()
        val context = Store<Unit>(engine).context
        val data = "Sample data"
        val externRef = ExternRef(data)

        val rawValue = externRef.toRaw(context)
        val convertedRef = ExternRef.fromRaw<String>(context, rawValue)

        assertNotNull(convertedRef, "ExternRef should be created successfully from raw value")
        assertEquals(data, convertedRef.data<String>(), "Wrapped data in the converted ExternRef should match the original data")

        externRef.close()
        convertedRef.close()
    }
}