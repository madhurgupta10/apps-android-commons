package fr.free.nrw.commons.core.network

/**
 * Base result class for network operations.
 * Represents the outcome of a network request with success or error states.
 *
 * @param T The type of data returned on success
 */
sealed class NetworkResult<out T> {
    /**
     * Represents a successful network request with data.
     */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * Represents a failed network request with error information.
     */
    data class Error(
        val message: String,
        val code: Int? = null,
        val throwable: Throwable? = null
    ) : NetworkResult<Nothing>()

    /**
     * Represents a loading state during the network request.
     */
    data object Loading : NetworkResult<Nothing>()

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
     * Check if the result is loading.
     */
    val isLoading: Boolean
        get() = this is Loading

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
    inline fun <R> map(transform: (T) -> R): NetworkResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    /**
     * Handle success and error cases.
     */
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String, Int?, Throwable?) -> Unit): NetworkResult<T> {
        if (this is Error) action(message, code, throwable)
        return this
    }

    inline fun onLoading(action: () -> Unit): NetworkResult<T> {
        if (this is Loading) action()
        return this
    }
}

/**
 * Extension function to safely execute a network call and wrap it in NetworkResult.
 */
suspend fun <T> safeNetworkCall(
    call: suspend () -> T
): NetworkResult<T> {
    return try {
        NetworkResult.Success(call())
    } catch (e: Exception) {
        NetworkResult.Error(
            message = e.message ?: "Unknown error occurred",
            throwable = e
        )
    }
}

