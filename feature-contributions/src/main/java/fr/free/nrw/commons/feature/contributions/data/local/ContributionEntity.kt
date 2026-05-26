package fr.free.nrw.commons.feature.contributions.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import fr.free.nrw.commons.feature.contributions.domain.model.ContributionModel
import java.util.Date

/**
 * Room entity for storing contributions in the local database
 */
@Entity(tableName = "user_contributions")
@TypeConverters(ContributionConverters::class)
data class ContributionEntity(
    @PrimaryKey
    val pageId: String,
    val username: String,
    val filename: String?,
    val thumbUrl: String?,
    val imageUrl: String?,
    val dateUploaded: Date?,
    val description: String?,
    val author: String?,
    val categories: List<String>,
    val state: Int,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun toModel(): ContributionModel {
        return ContributionModel(
            pageId = pageId,
            filename = filename,
            thumbUrl = thumbUrl,
            imageUrl = imageUrl,
            dateUploaded = dateUploaded,
            description = description,
            author = author,
            categories = categories,
            state = state
        )
    }

    companion object {
        fun fromModel(model: ContributionModel, username: String): ContributionEntity {
            return ContributionEntity(
                pageId = model.pageId,
                username = username,
                filename = model.filename,
                thumbUrl = model.thumbUrl,
                imageUrl = model.imageUrl,
                dateUploaded = model.dateUploaded,
                description = model.description,
                author = model.author,
                categories = model.categories,
                state = model.state
            )
        }
    }
}

