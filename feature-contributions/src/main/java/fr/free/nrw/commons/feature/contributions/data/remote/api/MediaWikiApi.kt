package fr.free.nrw.commons.feature.contributions.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * MediaWiki API interface for fetching user contributions
 */
interface MediaWikiApi {

    /**
     * Get images uploaded by a user
     */
    @GET("w/api.php?action=query&format=json&formatversion=2&generator=allimages&gaisort=timestamp&gaidir=older&prop=imageinfo|categories&iiprop=url|extmetadata|user|timestamp|size&iiurlwidth=640&iiextmetadatafilter=DateTime|Categories|GPSLatitude|GPSLongitude|ImageDescription|DateTimeOriginal|Artist|LicenseShortName|LicenseUrl")
    suspend fun getUserContributions(
        @Query("gaiuser") username: String,
        @Query("gailimit") itemLimit: Int = 10,
        @QueryMap(encoded = true) continuation: Map<String, String> = emptyMap()
    ): MediaWikiResponse
}

