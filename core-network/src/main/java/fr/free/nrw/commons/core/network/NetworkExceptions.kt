package fr.free.nrw.commons.core.network

/**
 * Common network exceptions for better error handling.
 */

/**
 * Exception thrown when a network request times out.
 */
class NetworkTimeoutException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Exception thrown when there's no internet connection.
 */
class NoConnectivityException(message: String = "No internet connection") :
    Exception(message)

/**
 * Exception thrown when the server returns an error response.
 */
class ServerException(
    val code: Int,
    message: String,
    cause: Throwable? = null
) : Exception("Server error ($code): $message", cause)

/**
 * Exception thrown when the API response cannot be parsed.
 */
class ParseException(message: String, cause: Throwable? = null) :
    Exception("Failed to parse response: $message", cause)

