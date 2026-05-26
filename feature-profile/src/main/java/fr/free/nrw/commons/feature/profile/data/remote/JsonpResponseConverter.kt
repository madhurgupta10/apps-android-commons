package fr.free.nrw.commons.feature.profile.data.remote

import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import timber.log.Timber
import java.lang.reflect.Type

/**
 * Converter factory for handling JSONP responses from Toolforge APIs.
 * The feedback.py and similar endpoints return JSON wrapped in JavaScript/JSONP format.
 * This converter extracts the JSON part before parsing.
 *
 * Example response format:
 * ```
 * some_callback({"user": "username", ...})
 * ```
 * or
 * ```
 * var data = {"user": "username", ...};
 * ```
 */
class JsonpResponseConverterFactory private constructor(
    private val gson: Gson
) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        val delegate = gson.getAdapter(com.google.gson.reflect.TypeToken.get(type))
        return JsonpResponseConverter(delegate)
    }

    private class JsonpResponseConverter<T>(
        private val delegate: com.google.gson.TypeAdapter<T>
    ) : Converter<ResponseBody, T> {
        override fun convert(value: ResponseBody): T? {
            val responseString = value.string()

            // Log raw response for debugging
            Timber.d("JsonpConverter: Raw response length: ${responseString.length}")
            if (responseString.length < 500) {
                Timber.d("JsonpConverter: Full response: $responseString")
            } else {
                Timber.d("JsonpConverter: Response (first 200 chars): ${responseString.take(200)}")
            }

            // Handle empty or very short responses
            if (responseString.isBlank() || responseString.length < 2) {
                Timber.e("JsonpConverter: Empty or too short response")
                // Return null instead of throwing - let the caller handle it
                return null
            }

            // Extract JSON from JSONP/JavaScript wrapper
            // Find the first occurrence of '{' which marks the start of JSON
            val jsonStartIndex = responseString.indexOf('{')

            if (jsonStartIndex == -1) {
                // No JSON found - check if it's an error message
                Timber.e("JsonpConverter: No JSON found in response")
                Timber.e("JsonpConverter: Response content: $responseString")

                // If response looks like an error (contains 'error', 'Error', etc.)
                if (responseString.contains("error", ignoreCase = true) ||
                    responseString.contains("exception", ignoreCase = true) ||
                    responseString.startsWith("Content-type", ignoreCase = true) ||
                    responseString.contains("<!DOCTYPE", ignoreCase = true) ||
                    responseString.contains("<html", ignoreCase = true)) {
                    Timber.e("JsonpConverter: Response appears to be an error/HTML page")
                    throw IllegalStateException("Toolforge API returned an error. Response: ${responseString.take(200)}")
                }

                throw IllegalStateException("No JSON object found in Toolforge response: ${responseString.take(100)}")
            }

            // Extract everything from first { to the end
            val jsonString = responseString.substring(jsonStartIndex)

            // Find the last '}' to handle cases like: callback({...});
            val lastBraceIndex = jsonString.lastIndexOf('}')
            val cleanJson = if (lastBraceIndex != -1 && lastBraceIndex < jsonString.length - 1) {
                // There's content after the last }, likely ");" or ";"
                jsonString.substring(0, lastBraceIndex + 1)
            } else {
                jsonString
            }

            Timber.d("JsonpConverter: Cleaned JSON length: ${cleanJson.length}")

            return try {
                delegate.fromJson(cleanJson)
            } catch (e: Exception) {
                Timber.e(e, "JsonpConverter: Failed to parse JSON: ${cleanJson.take(200)}")
                throw e
            }
        }
    }

    companion object {
        fun create(gson: Gson): JsonpResponseConverterFactory {
            return JsonpResponseConverterFactory(gson)
        }
    }
}

