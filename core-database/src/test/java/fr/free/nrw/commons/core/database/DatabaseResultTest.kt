package fr.free.nrw.commons.core.database

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for DatabaseResult
 */
class DatabaseResultTest {

    @Test
    fun `success result should have correct properties`() {
        val result = DatabaseResult.Success("test data")

        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertEquals("test data", result.getOrNull())
    }

    @Test
    fun `error result should have correct properties`() {
        val result = DatabaseResult.Error("error message", null)

        assertFalse(result.isSuccess)
        assertTrue(result.isError)
        assertNull(result.getOrNull())
    }

    @Test
    fun `map should transform success data`() {
        val result = DatabaseResult.Success(5)
        val mapped = result.map { it * 2 }

        assertTrue(mapped is DatabaseResult.Success)
        assertEquals(10, mapped.getOrNull())
    }

    @Test
    fun `map should preserve error`() {
        val result: DatabaseResult<Int> = DatabaseResult.Error("error")
        val mapped = result.map { it * 2 }

        assertTrue(mapped is DatabaseResult.Error)
        assertNull(mapped.getOrNull())
    }

    @Test
    fun `onSuccess should be called for success`() {
        var called = false
        var receivedData: String? = null

        DatabaseResult.Success("data")
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

        DatabaseResult.Error("error")
            .onSuccess { called = true }

        assertFalse(called)
    }

    @Test
    fun `onError should be called for error`() {
        var called = false
        var receivedMessage: String? = null

        DatabaseResult.Error("error message", null)
            .onError { message, _ ->
                called = true
                receivedMessage = message
            }

        assertTrue(called)
        assertEquals("error message", receivedMessage)
    }

    @Test
    fun `onError should not be called for success`() {
        var called = false

        DatabaseResult.Success("data")
            .onError { _, _ -> called = true }

        assertFalse(called)
    }
}

