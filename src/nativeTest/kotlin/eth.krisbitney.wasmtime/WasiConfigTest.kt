package eth.krisbitney.wasmtime

import kotlinx.cinterop.pointed
import kotlin.test.*

class WasiConfigTest {

    @Test
    fun testCreateWasiConfig() {
        val config = WasiConfig()
        assertNotNull(config)
        assertNotEquals(config.wasiConfig.rawValue.toLong(), 0)
        assertNotNull(config.wasiConfig.pointed)
        config.close()
    }

    @Test
    fun testSetArgv() {
        val config = WasiConfig()
        val argv = listOf("arg1", "arg2", "arg3")
        config.setArgv(argv)
        config.close()
    }

    @Test
    fun testInheritArgv() {
        val config = WasiConfig()
        config.inheritArgv()
        config.close()
    }

    @Test
    fun testSetEnv() {
        val config = WasiConfig()
        val env = mapOf("KEY1" to "VALUE1", "KEY2" to "VALUE2")
        config.setEnv(env)
        config.close()
    }

    @Test
    fun testInheritEnv() {
        val config = WasiConfig()
        config.inheritEnv()
        config.close()
    }

    @Test
    fun testSetStdinFile() {
        val config = WasiConfig()
        // TODO: how to dynamically get absolute path to test file?
        val path = "/Users/kris/IdeaProjects/krisbitney/wasmtime-kt/src/nativeTest/resources/mockWasiConfigIoFile.txt"
        val result = config.setStdinFile(path)
        assertTrue(result)
        config.close()
    }

    @Test
    fun testSetStdinBytes() {
        val config = WasiConfig()
        val bytes = "Test input".encodeToByteArray()
        config.setStdinBytes(bytes)
        config.close()
    }

    @Test
    fun testInheritStdin() {
        val config = WasiConfig()
        config.inheritStdin()
        config.close()
    }

    @Test
    fun testSetStdoutFile() {
        val config = WasiConfig()
        // TODO: how to dynamically get absolute path to test file?
        val path = "/Users/kris/IdeaProjects/krisbitney/wasmtime-kt/src/nativeTest/resources/mockWasiConfigIoFile.txt"
        val result = config.setStdoutFile(path)
        assertTrue(result)
        config.close()
    }

    @Test
    fun testInheritStdout() {
        val config = WasiConfig()
        config.inheritStdout()
        config.close()
    }

    @Test
    fun testSetStderrFile() {
        val config = WasiConfig()
        val path = "test_stderr_file.txt"
        val result = config.setStderrFile(path)
        assertTrue(result)
        config.close()
    }

    @Test
    fun testInheritStderr() {
        val config = WasiConfig()
        config.inheritStderr()
        config.close()
    }

    @Test
    fun testPreopenDir() {
        val config = WasiConfig()
        // TODO: how to dynamically get absolute path to test dir?
        val path = "/Users/kris/IdeaProjects/krisbitney/wasmtime-kt/src/nativeTest/resources"
        val guestPath = "test_guest_directory"
        val result = config.preopenDir(path, guestPath)
        assertTrue(result)
        config.close()
    }

    @Test
    fun testPreopenSocket() {
        val config = WasiConfig()
        val fdNum = 3u
        val hostPort = "127.0.0.1:8080"
        val result = config.preopenSocket(fdNum, hostPort)
        assertTrue(result)
        config.close()
    }
}
