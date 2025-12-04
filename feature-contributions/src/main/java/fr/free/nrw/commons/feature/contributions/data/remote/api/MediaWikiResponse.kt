package fr.free.nrw.commons.feature.contributions.data.remote.api

import com.google.gson.annotations.SerializedName

/**
 * Response from MediaWiki API
 */
data class MediaWikiResponse(
    @SerializedName("continue")
    val continueToken: Map<String, String>?,

    @SerializedName("query")
    val query: Query?
) {
    /**
     * Get the gaicontinue value from the continuation map
     */
    fun getGaiContinue(): String? = continueToken?.get("gaicontinue")

    /**
     * Check if there are more pages to fetch
     */
    fun hasMore(): Boolean = continueToken != null && getGaiContinue() != null
}


data class Query(
    @SerializedName("pages")
    val pages: List<Page>?
)

data class Page(
    @SerializedName("pageid")
    val pageId: Long?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("imageinfo")
    val imageInfo: List<ImageInfo>?,

    @SerializedName("categories")
    val categories: List<Category>?
)

data class ImageInfo(
    @SerializedName("url")
    val url: String?,

    @SerializedName("thumburl")
    val thumbUrl: String?,

    @SerializedName("timestamp")
    val timestamp: String?,

    @SerializedName("user")
    val user: String?,

    @SerializedName("size")
    val size: Long?,

    @SerializedName("extmetadata")
    val extMetadata: ExtMetadata?
)

data class ExtMetadata(
    @SerializedName("ImageDescription")
    val imageDescription: MetadataValue?,

    @SerializedName("Artist")
    val artist: MetadataValue?,

    @SerializedName("DateTime")
    val dateTime: MetadataValue?,

    @SerializedName("DateTimeOriginal")
    val dateTimeOriginal: MetadataValue?,

    @SerializedName("LicenseShortName")
    val licenseShortName: MetadataValue?,

    @SerializedName("LicenseUrl")
    val licenseUrl: MetadataValue?
)

data class MetadataValue(
    @SerializedName("value")
    val value: String?
)

data class Category(
    @SerializedName("title")
    val title: String?
)

