package fr.free.nrw.commons.core.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

/**
 * Core network configuration and factory for creating Retrofit instances.
 * This is a Kotlin library module (not Android-specific) to keep dependencies minimal.
 *
 * Usage:
 * ```
 * val retrofit = NetworkFactory.createRetrofit(
 *     baseUrl = "https://api.example.com/",
 *     enableLogging = true
 * )
 * val apiService = retrofit.create(YourApiService::class.java)
 * ```
 */
object NetworkFactory {

    /**
     * Default timeout for network requests
     */
    private const val DEFAULT_TIMEOUT = 30L

    /**
     * Creates a configured Retrofit instance.
     *
     * @param baseUrl Base URL for the API
     * @param okHttpClient Optional custom OkHttpClient. If null, a default one will be created
     * @param gson Optional custom Gson instance. If null, a default one will be created
     * @param enableLogging Enable HTTP logging interceptor
     * @return Configured Retrofit instance
     */
    fun createRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient? = null,
        gson: Gson? = null,
        enableLogging: Boolean = false,
        interceptors: List<Interceptor> = emptyList()
    ): Retrofit {
        val client = okHttpClient ?: createDefaultOkHttpClient(enableLogging, interceptors)
        val gsonInstance = gson ?: createDefaultGson()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gsonInstance))
            .build()
    }

    /**
     * Creates a default OkHttpClient with common configurations.
     *
     * @param enableLogging Enable HTTP logging interceptor
     * @param interceptors Additional interceptors to add
     * @return Configured OkHttpClient
     */
    fun createDefaultOkHttpClient(
        enableLogging: Boolean = false,
        interceptors: List<Interceptor> = emptyList()
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)

            // Add custom interceptors
            interceptors.forEach { addInterceptor(it) }

            // Add logging interceptor if enabled
            if (enableLogging) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
            }
        }.build()
    }

    /**
     * Creates a default Gson instance with common configurations.
     *
     * @return Configured Gson instance
     */
    fun createDefaultGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
}

