package fr.free.nrw.commons.feature.contributions.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * MediaWiki API interface for fetching user contributions and category media
 * API format EXACTLY matches the working implementation from app module's MediaInterface
 */
interface MediaWikiApi {

    /**
     * Get images uploaded by a user
     * Format: prop=imageinfo|coordinates, iiprop=url|extmetadata|user|timestamp, iiurlwidth (not height)
     * Note: timestamp is required in iiprop to get upload date for proper timeline display
     */
    @GET("w/api.php?action=query&format=json&formatversion=2&generator=allimages&gaisort=timestamp&gaidir=older&prop=imageinfo|coordinates&iiprop=url|extmetadata|user|timestamp&iiurlwidth=640&iiextmetadatafilter=DateTime|Categories|GPSLatitude|GPSLongitude|ImageDescription|DateTimeOriginal|Artist|LicenseShortName|LicenseUrl")
    suspend fun getUserContributions(
        @Query("gaiuser") username: String,
        @Query("gailimit") itemLimit: Int = 10,
        @QueryMap(encoded = true) continuation: Map<String, String> = emptyMap()
    ): MediaWikiResponse

    /**
     * Get images from a category
     * Uses categorymembers generator, same iiprop/iiurlwidth as getUserContributions for consistent thumbUrls
     */
    @GET("w/api.php?action=query&format=json&formatversion=2&generator=categorymembers&gcmtype=file&gcmsort=timestamp&gcmdir=desc&prop=imageinfo|coordinates&iiprop=url|extmetadata|user|timestamp&iiurlwidth=640&iiextmetadatafilter=DateTime|Categories|GPSLatitude|GPSLongitude|ImageDescription|DateTimeOriginal|Artist|LicenseShortName|LicenseUrl")
    suspend fun getCategoryMedia(
        @Query("gcmtitle") categoryTitle: String,
        @Query("gcmlimit") itemLimit: Int = 30,
        @QueryMap(encoded = true) continuation: Map<String, String> = emptyMap()
    ): MediaWikiResponse
}

