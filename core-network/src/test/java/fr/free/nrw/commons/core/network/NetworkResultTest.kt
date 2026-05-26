package fr.free.nrw.commons.core.network

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for NetworkResult
 */
class NetworkResultTest {

    @Test
    fun `success result should have correct properties`() {
        val result = NetworkResult.Success("test data")

        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertFalse(result.isLoading)
        assertEquals("test data", result.getOrNull())
    }

    @Test
    fun `error result should have correct properties`() {
        val result = NetworkResult.Error("error message", 404, null)

        assertFalse(result.isSuccess)
        assertTrue(result.isError)
        assertFalse(result.isLoading)
        assertNull(result.getOrNull())
    }

    @Test
    fun `loading result should have correct properties`() {
        val result = NetworkResult.Loading

        assertFalse(result.isSuccess)
        assertFalse(result.isError)
        assertTrue(result.isLoading)
        assertNull(result.getOrNull())
    }

    @Test
    fun `map should transform success data`() {
        val result = NetworkResult.Success(5)
        val mapped = result.map { it * 2 }

        assertTrue(mapped is NetworkResult.Success)
        assertEquals(10, mapped.getOrNull())
    }

    @Test
    fun `map should preserve error`() {
        val result: NetworkResult<Int> = NetworkResult.Error("error")
        val mapped = result.map { it * 2 }

        assertTrue(mapped is NetworkResult.Error)
        assertNull(mapped.getOrNull())
    }

    @Test
    fun `onSuccess should be called for success`() {
        var called = false
        var receivedData: String? = null

        NetworkResult.Success("data")
            .onSuccess {
                called = true
                receivedData = it
            }

        assertTrue(called)
        assertEquals("data", receivedData)
    }

    @Test
    fun `onSuccess should not be called for error`() {
        var called = false

        NetworkResult.Error("error")
            .onSuccess { called = true }

        assertFalse(called)
    }

    @Test
    fun `onError should be called for error`() {
        var called = false
        var receivedMessage: String? = null

        NetworkResult.Error("error message", 500, null)
            .onError { message, code, _ ->
                called = true
                receivedMessage = message
            }

        assertTrue(called)
        assertEquals("error message", receivedMessage)
    }

    @Test
    fun `onError should not be called for success`() {
        var called = false

        NetworkResult.Success("data")
            .onError { _, _, _ -> called = true }

        assertFalse(called)
    }
}

