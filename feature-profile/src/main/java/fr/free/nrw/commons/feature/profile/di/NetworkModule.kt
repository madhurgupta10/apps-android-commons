package fr.free.nrw.commons.feature.profile.di

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.free.nrw.commons.feature.profile.data.remote.JsonpResponseConverterFactory
import fr.free.nrw.commons.feature.profile.data.remote.ProfileApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module providing network dependencies for the profile feature.
 * Uses the OkHttpClient from the app module and core-network module for Retrofit configuration.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://tools.wmflabs.org/commons-android-app/tool-commons-android-app/"

    /**
     * Provides a Retrofit instance configured for profile API calls.
     * Uses a custom JSONP converter to handle Toolforge API responses.
     *
     * @param okHttpClient The singleton OkHttpClient from the app module
     * @param gson Gson instance from app's NetworkingModule (shared singleton)
     */
    @Provides
    @Singleton
    @Named("profile_retrofit")
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        // Toolforge APIs (feedback.py, leaderboard.py) return JSON wrapped in JavaScript
        // We need a custom converter to extract the JSON before parsing
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(JsonpResponseConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideProfileApiService(@Named("profile_retrofit") retrofit: Retrofit): ProfileApiService {
        return retrofit.create(ProfileApiService::class.java)
    }

    /**
     * Provides a custom ImageLoader for Coil with User-Agent header.
     * Required by Wikimedia servers to prevent HTTP 403 errors.
     */
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        // Create a custom OkHttpClient with User-Agent header for image loading
        val imageOkHttpClient = okHttpClient.newBuilder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", getUserAgent(context))
                    .build()
                chain.proceed(request)
            })
            .build()

        return ImageLoader.Builder(context)
            .okHttpClient(imageOkHttpClient)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .logger(DebugLogger())
            .respectCacheHeaders(false)
            .build()
    }

    /**
     * Generates the User-Agent string for HTTP requests.
     * Format: Commons/{versionName} (https://mediawiki.org/wiki/Apps/Commons) Android/{androidVersion}
     */
    private fun getUserAgent(context: Context): String {
        val versionName = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
        return "Commons/$versionName (https://mediawiki.org/wiki/Apps/Commons) Android/${Build.VERSION.RELEASE}"
    }
}

