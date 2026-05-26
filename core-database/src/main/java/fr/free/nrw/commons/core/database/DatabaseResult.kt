package fr.free.nrw.commons.core.database

/**
 * Result wrapper for database operations.
 * Represents the outcome of a database operation with success or error states.
 *
 * @param T The type of data returned on success
 */
sealed class DatabaseResult<out T> {
    /**
     * Represents a successful database operation with data.
     */
    data class Success<T>(val data: T) : DatabaseResult<T>()

    /**
     * Represents a failed database operation with error information.
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : DatabaseResult<Nothing>()

    /**
     * Check if the result is successful.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Check if the result is an error.
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Get the data if successful, or null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Transform the success data.
     */
    inline fun <R> map(transform: (T) -> R): DatabaseResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    /**
     * Handle success and error cases.
     */
    inline fun onSuccess(action: (T) -> Unit): DatabaseResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String, Throwable?) -> Unit): DatabaseResult<T> {
        if (this is Error) action(message, throwable)
        return this
    }
}

/**
 * Extension function to safely execute a database operation and wrap it in DatabaseResult.
 */
suspend fun <T> safeDatabaseCall(
    call: suspend () -> T
): DatabaseResult<T> {
    return try {
        DatabaseResult.Success(call())
    } catch (e: Exception) {
        DatabaseResult.Error(
            message = e.message ?: "Database error occurred",
            throwable = e
        )
    }
}

