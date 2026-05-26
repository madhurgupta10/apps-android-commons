package fr.free.nrw.commons.feature.profile.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for user profile API response.
 * Based on the actual /feedback.py API response structure.
 */
data class UserProfileDto(
    @SerializedName("user")
    val username: String = "",

    @SerializedName("uniqueUsedImages")
    val uniqueImagesUsed: Int = 0,

    @SerializedName("articlesUsingImages")
    val articlesUsingImages: Int = 0,

    @SerializedName("deletedUploads")
    val deletedUploads: Int = 0,

    @SerializedName("thanksReceived")
    val thanksReceived: Int = 0,

    @SerializedName("featuredImages")
    val featuredImages: FeaturedImagesDto = FeaturedImagesDto()
) {
    // Derived properties not directly from API
    val avatar: String? = null // API doesn't provide avatar URL
    val rank: Int = 0 // API doesn't provide rank directly
    val uploadCount: Int = uniqueImagesUsed + deletedUploads // Estimated total uploads
}

data class FeaturedImagesDto(
    @SerializedName("Quality_images")
    val qualityImages: Int = 0,

    @SerializedName("Featured_pictures_on_Wikimedia_Commons")
    val featuredPicturesOnWikimediaCommons: Int = 0
)

